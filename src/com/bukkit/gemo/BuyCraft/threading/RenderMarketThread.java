package com.bukkit.gemo.BuyCraft.threading;

import java.awt.Color;
import java.awt.Graphics;
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
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;

import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.BCMinimalItem;
import com.bukkit.gemo.BuyCraft.BCUserShop;
import com.bukkit.gemo.BuyCraft.MarketArea;

public class RenderMarketThread implements Runnable {

    private static int TEXTURE_BLOCK_SIZE = 32;
    private TreeMap<String, ChunkSnapshot> snapList;
    private HashMap<String, BCUserShop> userShopList;
    private static HashMap<String, BufferedImage> imageList;
    private MarketArea market;
    private String playerName;

    @SuppressWarnings("unchecked")
    public RenderMarketThread(final String playerName, final TreeMap<String, ChunkSnapshot> chunkList, final HashMap<String, BCUserShop> userShopList, MarketArea market) {
        this.playerName = playerName;
        this.snapList = (TreeMap<String, ChunkSnapshot>) chunkList.clone();
        this.userShopList = (HashMap<String, BCUserShop>) userShopList.clone();
        this.market = market.clone();
    }

    // /////////////////////////////////
    //
    // RENDERING THREAD
    //
    // /////////////////////////////////
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            // CREATE IMAGE
            BufferedImage image = new BufferedImage(market.getAreaBlockWidth() * TEXTURE_BLOCK_SIZE, market.getAreaBlockLength() * TEXTURE_BLOCK_SIZE, BufferedImage.TYPE_INT_RGB);

            File outputDir = new File("plugins/BuyCraft/markets/");
            outputDir.mkdir();
            outputDir = new File("plugins/BuyCraft/markets/tiles/");
            outputDir.mkdir();

            File output = new File("plugins/BuyCraft/markets/" + market.getAreaName() + ".png");
            if (output.exists())
                output.delete();

            output.createNewFile();

            // ///////////////////////////////////////////
            // BEGIN PAINTING
            // Method 1 : Iterate through all blocks (blockwise drawing)
            // Method 2 : Iterate through chunklist (chunkwise drawing)
            // NOTE: Method 2 seems to be faster, so we use it
            // ///////////////////////////////////////////
            this.renderWithMethod2(image);
            // ///////////////////////////////////////////
            // END PAINTING
            // ///////////////////////////////////////////

            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarketFinishedThread(playerName, ChatColor.GREEN + "Terraingeneration of '" + market.getAreaName() + "' finished in " + duration + "ms"), 1);

            // CREATE TILES
            createAllTiles(image, 256);

            // SAVE FILE
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarketFinishedThread(playerName, ChatColor.GREEN + "Saving large image..."), 1);
            startTime = System.currentTimeMillis();
            ImageIO.write(image, "png", output);
            image.flush();
            duration = System.currentTimeMillis() - startTime;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarketFinishedThread(playerName, ChatColor.GREEN + "Large image saved to HDD in " + duration + "ms."), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        exportMarketHtmlPage(market);        

        // COLLECT GARBAGE
        snapList.clear();
        market = null;
    }

    // /////////////////////////////////
    //
    // CREATE TILE-SET
    //
    // /////////////////////////////////
    public void createAllTiles(BufferedImage image, final int tileSize) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarketFinishedThread(playerName, ChatColor.GREEN + "Creating tiles..."), 1);
        int maxTilesX = (int) (image.getWidth() / tileSize);
        int maxTilesZ = (int) (image.getHeight() / tileSize);

        int cutRight = ((maxTilesX + 1) * tileSize) - image.getWidth();
        int cutBottom = ((maxTilesZ + 1) * tileSize) - image.getHeight();

        BufferedImage tileImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
        Graphics tileGraphic = tileImage.getGraphics();
        File f;

        tileGraphic.setColor(Color.BLACK);
        int tileCount = 0;
        long startTime = System.currentTimeMillis();
        for (int x = 0; x <= maxTilesX; x++) {
            for (int z = 0; z <= maxTilesZ; z++) {                
                try {
                    // DRAW IMAGE
                    tileGraphic.drawImage(image, -(x * 256), -(z * 256), null);
                    
                    // CREATE TILE-FILE
                    f = new File("plugins/BuyCraft/markets/tiles/" + x + "_" + z + ".png");
                    if (f.exists())
                        f.delete();
                    f.createNewFile();

                    // REPAINT THE SIDES, IF NEEDED
                    if (x == maxTilesX && z == maxTilesZ) {
                        // CUT OFF RIGHT AND BOTTOM
                        tileGraphic.fillRect(tileSize - cutRight, 0, cutRight, tileSize);
                        tileGraphic.fillRect(0, tileSize - cutBottom, tileSize, cutBottom);
                    }
                    else if (x == maxTilesX) {
                        // CUT OFF RIGHT
                        tileGraphic.fillRect(tileSize - cutRight, 0, cutRight, tileSize);
                    }
                    else if (z == maxTilesZ) {
                        // CUT OF BOTTOM
                        tileGraphic.fillRect(0, tileSize - cutBottom, tileSize, cutBottom);
                    }
                    
                    // SAVE FILE TO HDD
                    ImageIO.write(tileImage, "png", f);
                    tileCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }                
            }
        }
        long duration = System.currentTimeMillis() - startTime;        
        float timePerTile = (float)duration / (float)tileCount;        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarketFinishedThread(playerName, ChatColor.GREEN + "Tilecreation finished in " + duration + "ms. (Aprox. " + timePerTile + "ms per tile)"), 1);
    }

    // ///////////////////////////////////////////
    //
    // METHOD 1 - ITERATE THROUGH ALL BLOCKS
    //
    // ///////////////////////////////////////////
    public void renderWithMethod1(BufferedImage image) {
        Graphics2D graphic = (Graphics2D) image.getGraphics();
        int blockChunkX;
        int blockChunkZ;
        int blockX, blockZ;
        int highestY;

        ArrayList<BCMinimalItem> queuedBlocks = new ArrayList<BCMinimalItem>();

        int TypeID, SubID;
        String chunkString = "";
        int textureX = 0;
        int textureZ = 0;
        for (int x = 0; x < market.getAreaBlockWidth(); x++) {
            blockX = (x + market.getCorner1().getBlockX()) & 0xF;
            blockChunkX = (x + market.getCorner1().getBlockX()) >> 4;
            textureZ = 0;
            for (int z = 0; z < market.getAreaBlockLength(); z++) {
                blockZ = (z + market.getCorner1().getBlockZ()) & 0xF;
                blockChunkZ = (z + market.getCorner1().getBlockZ()) >> 4;
                chunkString = blockChunkX + "_" + blockChunkZ;

                highestY = snapList.get(chunkString).getHighestBlockYAt(blockX, blockZ);
                if (highestY > market.getCorner2().getBlockY())
                    highestY = market.getCorner2().getBlockY();

                TypeID = snapList.get(chunkString).getBlockTypeId(blockX, highestY, blockZ);

                while (isTextureTransparent(TypeID) && highestY > 0) {
                    SubID = snapList.get(chunkString).getBlockData(blockX, highestY, blockZ);
                    queuedBlocks.add(new BCMinimalItem(TypeID, SubID));
                    highestY--;
                    TypeID = snapList.get(chunkString).getBlockTypeId(blockX, highestY, blockZ);
                }

                SubID = snapList.get(chunkString).getBlockData(blockX, highestY, blockZ);
                this.drawBlock(graphic, textureX, textureZ, image.getWidth(), image.getHeight(), TypeID, SubID);
                // DRAW QUEUED BLOCKS
                for (int i = queuedBlocks.size() - 1; i >= 0; i--) {
                    this.drawBlock(graphic, textureX, textureZ, image.getWidth(), image.getHeight(), queuedBlocks.get(i).getID(), queuedBlocks.get(i).getSubID());
                }
                queuedBlocks.clear();
                textureZ += TEXTURE_BLOCK_SIZE;
            }
            textureX += TEXTURE_BLOCK_SIZE;
        }
    }

    // ///////////////////////////////////////////
    //
    // METHOD 2 - ITERATE THROUGH CHUNKLIST
    //
    // ///////////////////////////////////////////
    public void renderWithMethod2(BufferedImage image) {
        Graphics2D graphic = (Graphics2D) image.getGraphics();
        int minChunkX = market.getCorner1().getBlock().getChunk().getX();
        int minChunkZ = market.getCorner1().getBlock().getChunk().getZ();
        int maxChunkX = market.getCorner2().getBlock().getChunk().getX();
        int maxChunkZ = market.getCorner2().getBlock().getChunk().getZ();

        int startPosX, startPosZ;
        int offsetBlockX = market.getCorner1().getBlockX() & 0xF;
        int offsetBlockZ = market.getCorner1().getBlockZ() & 0xF;
        startPosX = 0 - (offsetBlockX * TEXTURE_BLOCK_SIZE);
        startPosZ = 0 - (offsetBlockZ * TEXTURE_BLOCK_SIZE);
        int chunkSize = 16 * TEXTURE_BLOCK_SIZE;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            startPosZ = 0 - (offsetBlockZ * TEXTURE_BLOCK_SIZE);
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                this.drawCompleteChunk(graphic, snapList.get(x + "_" + z), startPosX, startPosZ, image.getWidth(), image.getHeight());
                startPosZ += chunkSize;
            }
            startPosX += chunkSize;
        }
    }

    // /////////////////////////////////
    //
    // DRAW COMPLETE CHUNK
    //
    // /////////////////////////////////
    private void drawCompleteChunk(Graphics2D graphic, ChunkSnapshot snapshot, int startPosX, int startPosZ, final int maxWidth, final int maxHeight) {
        int TypeID, SubID;
        int textureX = startPosX;
        int textureZ = startPosZ;
        int highestY = 0;
        ArrayList<BCMinimalItem> queuedBlocks = new ArrayList<BCMinimalItem>();
        for (int blockX = 0; blockX < 16; blockX++) {
            textureZ = startPosZ;
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                highestY = snapshot.getHighestBlockYAt(blockX, blockZ);
                if (highestY > market.getCorner2().getBlockY())
                    highestY = market.getCorner2().getBlockY();

                TypeID = snapshot.getBlockTypeId(blockX, highestY, blockZ);

                while (isTextureTransparent(TypeID) && highestY > 0) {
                    SubID = snapshot.getBlockData(blockX, highestY, blockZ);
                    queuedBlocks.add(new BCMinimalItem(TypeID, SubID));
                    highestY--;
                    TypeID = snapshot.getBlockTypeId(blockX, highestY, blockZ);
                }

                SubID = snapshot.getBlockData(blockX, highestY, blockZ);
                this.drawBlock(graphic, textureX, textureZ, maxWidth, maxHeight, TypeID, SubID);
                // DRAW QUEUED BLOCKS
                for (int i = queuedBlocks.size() - 1; i >= 0; i--) {
                    this.drawBlock(graphic, textureX, textureZ, maxWidth, maxHeight, queuedBlocks.get(i).getID(), queuedBlocks.get(i).getSubID());
                }
                queuedBlocks.clear();
                textureZ += TEXTURE_BLOCK_SIZE;
            }
            textureX += TEXTURE_BLOCK_SIZE;
        }
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
    private void drawBlock(Graphics2D graphic, final int x, final int z, final int maxWidth, final int maxHeight, final int TypeID, final int SubID) {
        if (x < 0 || z < 0 || x > maxWidth || z > maxHeight)
            return;

        String picture = "" + TypeID;
        if (TypeID == 17 || TypeID == 35 || TypeID == 43 || TypeID == 44 || TypeID == 50 || TypeID == 63 || TypeID == 65 || TypeID == 66 || TypeID == 68 || TypeID == 75 || TypeID == 76 || TypeID == 98) {
            picture += "-" + SubID;
        }

        BufferedImage blockTex = imageList.get(picture);
        if (blockTex != null) {
            graphic.drawImage(blockTex, x, z, null);
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
    public static void loadTextures(int texSize) {
        TEXTURE_BLOCK_SIZE = texSize;
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
                    image = resize(image, TEXTURE_BLOCK_SIZE, TEXTURE_BLOCK_SIZE);
                    imageList.put(file.getCanonicalFile().getName().replace(".png", ""), image);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // /////////////////////////////////
    //
    // RESIZE IMAGE
    //
    // /////////////////////////////////
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TRANSLUCENT);
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }

    // /////////////////////////////////
    //
    // SAVE PAGE
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
