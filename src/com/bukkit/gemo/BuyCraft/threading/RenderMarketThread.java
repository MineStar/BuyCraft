package com.bukkit.gemo.BuyCraft.threading;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;

import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.BCMinimalItem;
import com.bukkit.gemo.BuyCraft.BCUserShop;
import com.bukkit.gemo.BuyCraft.MarketArea;

public class RenderMarketThread implements Runnable {

    private int TEXTURE_BLOCK_SIZE = 32;
    private TreeMap<String, ChunkSnapshot> snapList;
    private HashMap<String, BCUserShop> userShopList;
    private HashMap<String, BufferedImage> imageList;
    private MarketArea market;
    private String playerName;

    @SuppressWarnings("unchecked")
    public RenderMarketThread(final String playerName, final TreeMap<String, ChunkSnapshot> chunkList, final HashMap<String, BCUserShop> userShopList, int texSize, MarketArea market) {
        this.loadTextures();
        this.playerName = playerName;
        this.snapList = (TreeMap<String, ChunkSnapshot>) chunkList.clone();
        this.userShopList = (HashMap<String, BCUserShop>) userShopList.clone();
        this.TEXTURE_BLOCK_SIZE = texSize;
        this.market = market.clone();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            // CREATE IMAGE
            BufferedImage image = new BufferedImage(market.getAreaBlockWidth() * TEXTURE_BLOCK_SIZE, market.getAreaBlockLength() * TEXTURE_BLOCK_SIZE, BufferedImage.TRANSLUCENT);

            File outputDir = new File("plugins/BuyCraft/markets/");
            outputDir.mkdir();
            File output = new File("plugins/BuyCraft/markets/" + market.getAreaName() + ".png");
            output.createNewFile();

            Graphics2D graphic = (Graphics2D) image.getGraphics();
            graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // PAINT AREA
            int blockChunkX;
            int blockChunkZ;
            int blockX, blockZ;
            int highestY;

            ArrayList<BCMinimalItem> queuedBlocks = new ArrayList<BCMinimalItem>();
            int TypeID, SubID;
            for (int x = 0; x < market.getAreaBlockWidth(); x++) {
                blockX = (x + market.getCorner1().getBlockX()) & 0xF;
                blockChunkX = (x + market.getCorner1().getBlockX()) >> 4;
                for (int z = 0; z < market.getAreaBlockLength(); z++) {
                    blockZ = (z + market.getCorner1().getBlockZ()) & 0xF;
                    blockChunkZ = (z + market.getCorner1().getBlockZ()) >> 4;
                    highestY = snapList.get(blockChunkX + "_" + blockChunkZ).getHighestBlockYAt(blockX, blockZ);
                    if (highestY > market.getCorner2().getBlockY())
                        highestY = market.getCorner2().getBlockY();

                    TypeID = snapList.get(blockChunkX + "_" + blockChunkZ).getBlockTypeId(blockX, highestY, blockZ);
                    while (isTextureTransparent(TypeID) && highestY > 0) {
                        SubID = snapList.get(blockChunkX + "_" + blockChunkZ).getBlockData(blockX, highestY, blockZ);
                        queuedBlocks.add(new BCMinimalItem(TypeID, SubID));
                        highestY--;
                        TypeID = snapList.get(blockChunkX + "_" + blockChunkZ).getBlockTypeId(blockX, highestY, blockZ);
                    }
                    SubID = snapList.get(blockChunkX + "_" + blockChunkZ).getBlockData(blockX, highestY, blockZ);
                    this.drawBlock(graphic, x, z, TypeID, SubID);
                    // DRAW QUEUED BLOCKS
                    for (int i = queuedBlocks.size() - 1; i >= 0; i--) {
                        this.drawBlock(graphic, x, z, queuedBlocks.get(i).getID(), queuedBlocks.get(i).getSubID());
                    }
                    queuedBlocks.clear();
                }
            }

            // SAVE FILE
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // COLLECT GARBAGE
        snapList.clear();
        imageList.clear();
        market = null;

        exportMarketHtmlPage(market);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;       
       
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarketFinishedThread(duration, playerName, market.getAreaName()), 1);
    }

    // /////////////////////////////////
    //
    // EXPORT HTML-PAGE
    //
    // /////////////////////////////////
    private void exportMarketHtmlPage(MarketArea area) {
        BufferedReader reader = null;
        ArrayList<String> lineList = new ArrayList<String>();
        try {
            if (!new File("plugins/BuyCraft/markets/template.html").exists())
                return;

            reader = new BufferedReader(new FileReader(new File("plugins/BuyCraft/markets/template.html")));
            String text = null;
            while ((text = reader.readLine()) != null) {
                lineList.add(text.replace("\r\n", ""));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        StringBuilder areaBuilder = new StringBuilder();
        StringBuilder detailBuilder = new StringBuilder();
        HashMap<String, Integer> shopCount = new HashMap<String, Integer>();
        for (BCUserShop shop : userShopList.values()) {
            if (shop.isActive() && shop.getSign() != null) {
                if (area.isBlockInArea(shop.getSign().getBlock().getLocation())) {
                    int relX = shop.getX() - area.getCorner1().getBlockX();
                    int relZ = shop.getZ() - area.getCorner1().getBlockZ();
                    int thisID = 1;
                    if (shopCount.containsKey(shop.getShopOwner()))
                        thisID = shopCount.get(shop.getShopOwner());

                    areaBuilder.append(shop.getHTML_Area(thisID, relX, relZ, TEXTURE_BLOCK_SIZE));
                    detailBuilder.append(shop.getHTML_ShopDetails(thisID));

                    thisID++;
                    shopCount.put(shop.getShopOwner(), thisID);
                }
            }
        }
        lineList = this.replaceText(lineList, "%PICTURE%", area.getAreaName() + ".png");
        lineList = this.replaceText(lineList, "%AREATEXT%", areaBuilder.toString());
        lineList = this.replaceText(lineList, "%SHOPDETAILS%", detailBuilder.toString());
        this.savePage("plugins/BuyCraft/markets/" + area.getAreaName() + ".html", lineList);
    }

    // /////////////////////////////////
    //
    // DRAW BLOCK
    //
    // /////////////////////////////////
    private void drawBlock(Graphics2D graphic, int x, int z, int TypeID, int SubID) {
        String picture = "" + TypeID;
        if (TypeID == 17 || TypeID == 35 || TypeID == 43 || TypeID == 44 || TypeID == 50 || TypeID == 63 || TypeID == 65 || TypeID == 66 || TypeID == 68 || TypeID == 75 || TypeID == 76 || TypeID == 98) {
            picture += "-" + SubID;
        }

        BufferedImage blockTex = imageList.get(picture);
        if (blockTex != null) {
            graphic.drawImage(blockTex, x * TEXTURE_BLOCK_SIZE, z * TEXTURE_BLOCK_SIZE, TEXTURE_BLOCK_SIZE, TEXTURE_BLOCK_SIZE, null);
        }
        blockTex = null;
    }

    // /////////////////////////////////
    //
    // IS TEXTURE TRANSPARENT
    //
    // /////////////////////////////////
    public boolean isTextureTransparent(final int ID) {
        switch (ID) {
            case 0 : {
                return true;
            }
            case 6 : {
                return true;
            }
            case 8 : {
                return true;
            }
            case 9 : {
                return true;
            }
            case 18 : {
                return true;
            }
            case 20 : {
                return true;
            }
            case 27 : {
                return true;
            }
            case 28 : {
                return true;
            }
            case 30 : {
                return true;
            }
            case 31 : {
                return true;
            }
            case 32 : {
                return true;
            }
            case 37 : {
                return true;
            }
            case 38 : {
                return true;
            }
            case 39 : {
                return true;
            }
            case 40 : {
                return true;
            }
            case 50 : {
                return true;
            }
            case 51 : {
                return true;
            }
            case 52 : {
                return true;
            }
            case 55 : {
                return true;
            }
            case 59 : {
                return true;
            }
            case 63 : {
                return true;
            }
            case 65 : {
                return true;
            }
            case 66 : {
                return true;
            }
            case 68 : {
                return true;
            }
            case 69 : {
                return true;
            }
            case 75 : {
                return true;
            }
            case 76 : {
                return true;
            }
            case 79 : {
                return true;
            }
            case 81 : {
                return true;
            }
            case 83 : {
                return true;
            }
            case 85 : {
                return true;
            }
            case 90 : {
                return true;
            }
            case 96 : {
                return true;
            }
            case 101 : {
                return true;
            }
            case 102 : {
                return true;
            }
            case 107 : {
                return true;
            }
            case 111 : {
                return true;
            }
            case 113 : {
                return true;
            }
            case 115 : {
                return true;
            }
            case 117 : {
                return true;
            }
        }
        return false;
    }

    // /////////////////////////////////
    //
    // LOAD TEXTURES
    //
    // /////////////////////////////////
    private void loadTextures() {
        imageList = new HashMap<String, BufferedImage>();

        File dir = new File("plugins/BuyCraft/textures/");
        if (!dir.exists())
            return;

        File[] fileList = dir.listFiles();
        for (File file : fileList) {
            if (!file.isFile())
                continue;

            if (!file.getName().endsWith(".png"))
                continue;

            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    imageList.put(file.getCanonicalFile().getName().replace(".png", ""), image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // /////////////////////////////////
    //
    // SAVE TEXTURES
    //
    // /////////////////////////////////
    private void savePage(String fileName, ArrayList<String> lines) {
        try {
            File datei = new File(fileName);
            if (datei.exists()) {
                datei.delete();
            }
            File savedFile = new File(fileName);
            FileWriter writer = new FileWriter(savedFile, false);
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i) + System.getProperty("line.separator"));
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            BCCore.printInConsole("Error while writing Page: " + fileName);
        }
    }

    // /////////////////////////////////
    //
    // REPLACE TEXT
    //
    // /////////////////////////////////
    private ArrayList<String> replaceText(ArrayList<String> lines, String placeHolder, String newText) {
        String line = "";
        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
            if (line.contains(placeHolder)) {
                lines.set(i, line.replace(placeHolder, newText));
            }
        }
        return lines;
    }
}
