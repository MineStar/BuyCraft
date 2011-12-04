package com.bukkit.gemo.BuyCraft.threading;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
import org.bukkit.Location;

import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.BCMinimalItem;
import com.bukkit.gemo.BuyCraft.BCUserShop;
import com.bukkit.gemo.BuyCraft.MarketArea;

public class RenderMarketThread implements Runnable {

    private static int[] ZOOM_TEXTURE_SIZE = {32, 24, 16, 8};
    private TreeMap<String, ChunkSnapshot> snapList;
    private HashMap<String, BCUserShop> userShopList;
    private static HashMap<Integer, HashMap<String, BufferedImage>> imageList;
    private MarketArea market;
    private String playerName;
    private boolean renderAll = true;
    private Location changedLocation = null;
    private int[] realZoomLevels = {3, 2, 1, 0};

    private int maxTilesX, maxTilesZ, cutRight, cutBottom;

    private static final int TILE_SIZE = 256;

    // ////////////////////////////////////////////
    //
    // CONSTRUCTOR FOR FULL-RENDERING
    //
    // ////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public RenderMarketThread(final String playerName, final TreeMap<String, ChunkSnapshot> chunkList, final HashMap<String, BCUserShop> userShopList, MarketArea market) {
        this.playerName = playerName;
        this.snapList = (TreeMap<String, ChunkSnapshot>) chunkList.clone();
        this.userShopList = (HashMap<String, BCUserShop>) userShopList.clone();
        this.market = market.clone();
    }

    // ////////////////////////////////////////////
    //
    // CONSTRUCTOR FOR SINGLE-TILE-RENDERING
    //
    // ////////////////////////////////////////////
    public RenderMarketThread(final String playerName, final Location changedLocation, final TreeMap<String, ChunkSnapshot> chunkList, final HashMap<String, BCUserShop> userShopList, MarketArea market) {
        this(playerName, chunkList, userShopList, market);
        this.changedLocation = changedLocation.clone();
        this.renderAll = false;
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
            BufferedImage image = new BufferedImage(market.getAreaBlockWidth() * ZOOM_TEXTURE_SIZE[0], market.getAreaBlockLength() * ZOOM_TEXTURE_SIZE[0], BufferedImage.TYPE_INT_RGB);

            File outputDir = new File("plugins/BuyCraft/markets/");
            outputDir.mkdir();
            outputDir = new File("plugins/BuyCraft/markets/tiles/");
            outputDir.mkdir();

            File output = new File("plugins/BuyCraft/markets/" + market.getAreaName() + ".png");
            if (output.exists())
                output.delete();

            output.createNewFile();

            maxTilesX = (int) (image.getWidth() / TILE_SIZE);
            maxTilesZ = (int) (image.getHeight() / TILE_SIZE);

            cutRight = ((maxTilesX + 1) * TILE_SIZE) - image.getWidth();
            cutBottom = ((maxTilesZ + 1) * TILE_SIZE) - image.getHeight();

            if (renderAll) {
                // ////////////////////////////
                // CREATE A FULLRENDER
                // ////////////////////////////

                // ///////////////////////////////////////////
                // BEGIN PAINTING
                // Method 1 : Iterate through all blocks (blockwise drawing)
                // Method 2 : Iterate through chunklist (chunkwise drawing)
                // NOTE: Method 2 seems to be faster, so we use it
                // ///////////////////////////////////////////
                this.renderWithMethod2(image, 0);
                // ///////////////////////////////////////////
                // END PAINTING
                // ///////////////////////////////////////////

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Terraingeneration of '" + market.getAreaName() + "' finished in " + duration + "ms"), 1);

                // CREATE TILES
                createAllTiles(image, 0);

                // CREATE ZOOMED TILES
                BufferedImage zoomLevel1 = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[1] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[1] * market.getAreaBlockLength());
                BufferedImage zoomLevel2 = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[2] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[2] * market.getAreaBlockLength());
                BufferedImage zoomLevel3 = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[3] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[3] * market.getAreaBlockLength());
                createAllTiles(zoomLevel1, 1);
                createAllTiles(zoomLevel2, 2);
                createAllTiles(zoomLevel3, 3);

                // SAVE LARGE IMAGE
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Saving large image..."), 1);
                startTime = System.currentTimeMillis();
                ImageIO.write(image, "png", output);
                image.flush();
                duration = System.currentTimeMillis() - startTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Large image saved to HDD in " + duration + "ms."), 1);
            } else {
                // ////////////////////////////
                // SPECIFIC TILE-RENDER
                // ////////////////////////////

                // WE NEED A LOCATION
                if (changedLocation == null)
                    return;
                // SAME WORLD?
                if (!changedLocation.getWorld().getName().equalsIgnoreCase(market.getCorner1().getWorld().getName()))
                    return;

                // GET TILE POSITION
                int singleTileX = 0, singleTileZ = 0;
                int distX = changedLocation.getBlockX() - market.getCorner1().getBlockX();
                int distZ = changedLocation.getBlockZ() - market.getCorner1().getBlockZ();
                singleTileX = (int) (distX * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);
                singleTileZ = (int) (distZ * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);

                int nextSingleTileX = (int) ((distX + 1) * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);
                int nextSingleTileZ = (int) ((distZ + 1) * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);

                // 8 / 32 = 0.25
                double SmallBigRatio = (double) ZOOM_TEXTURE_SIZE[3] / (double) ZOOM_TEXTURE_SIZE[0];
                // 32 / 8 = 4
                int BigSmallRatio = (int) ZOOM_TEXTURE_SIZE[0] / ZOOM_TEXTURE_SIZE[3];

                double tempX = (double) singleTileX * SmallBigRatio;
                double tempZ = (double) singleTileZ * SmallBigRatio;

                int minChunkX = ((int) tempX) * BigSmallRatio;
                int minChunkZ = ((int) tempZ) * BigSmallRatio;
                int maxChunkX = minChunkX + BigSmallRatio;
                int maxChunkZ = minChunkZ + BigSmallRatio;
                minChunkX--;
                minChunkZ--;

                if (singleTileX < 0 || singleTileZ < 0 || singleTileX > maxTilesX || singleTileZ > maxTilesZ) {
                    return;
                }

                this.renderWithMethod2(image, 0);
                //this.renderSpecificRegionWithMethod2(image, new Point(minChunkX, minChunkZ), new Point(maxChunkX, maxChunkZ), 0);
                
                BufferedImage zoomLevel1 = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[1] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[1] * market.getAreaBlockLength());
                BufferedImage zoomLevel2 = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[2] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[2] * market.getAreaBlockLength());
                BufferedImage zoomLevel3 = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[3] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[3] * market.getAreaBlockLength());

                // EXPORT SINGLE TILE
                createSingleTile(image, singleTileX, singleTileZ, 0);
                createSingleTile(zoomLevel1, singleTileX, singleTileZ, 1);
                createSingleTile(zoomLevel2, singleTileX, singleTileZ, 2);
                createSingleTile(zoomLevel3, singleTileX, singleTileZ, 3);

                // BLOCK ON TILE-BORDER?
                if (nextSingleTileX != singleTileX || nextSingleTileZ != singleTileZ) {
                    if (nextSingleTileX <= maxTilesX || nextSingleTileZ <= maxTilesZ) {
                        createSingleTile(image, nextSingleTileX, nextSingleTileZ, 0);
                        createSingleTile(zoomLevel1, nextSingleTileX, nextSingleTileZ, 1);
                        createSingleTile(zoomLevel2, nextSingleTileX, nextSingleTileZ, 2);
                        createSingleTile(zoomLevel3, nextSingleTileX, nextSingleTileZ, 3);
                    }
                }

                // SAVE LARGE IMAGE
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Saving large image..."), 1);
                startTime = System.currentTimeMillis();
                ImageIO.write(image, "png", output);
                image.flush();
                long duration = System.currentTimeMillis() - startTime;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Large image saved to HDD in " + duration + "ms."), 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        exportMarketHtmlPage(market, 0);

        // COLLECT GARBAGE
        snapList.clear();
        market = null;
    }

    // /////////////////////////////////
    //
    // CREATE TILE-SET
    //
    // /////////////////////////////////
    public void createAllTiles(BufferedImage image, int zoomLevel) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Creating tiles... (zoomlevel " + zoomLevel + " )"), 1);

        maxTilesX = (int) (image.getWidth() / TILE_SIZE);
        maxTilesZ = (int) (image.getHeight() / TILE_SIZE);

        cutRight = ((maxTilesX + 1) * TILE_SIZE) - image.getWidth();
        cutBottom = ((maxTilesZ + 1) * TILE_SIZE) - image.getHeight();

        BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics tileGraphic = tileImage.getGraphics();
        File f;

        tileGraphic.setColor(Color.BLACK);
        int tileCount = 0;
        long startTime = System.currentTimeMillis();
        for (int x = 0; x <= maxTilesX; x++) {
            for (int z = 0; z <= maxTilesZ; z++) {
                try {
                    // DRAW IMAGE
                    tileGraphic.drawImage(image, -(x * TILE_SIZE), -(z * TILE_SIZE), null);

                    // CREATE TILE-FILE
                    f = new File("plugins/BuyCraft/markets/tiles/" + realZoomLevels[zoomLevel] + "_" + x + "_" + z + ".png");
                    if (f.exists())
                        f.delete();
                    f.createNewFile();

                    // REPAINT THE SIDES, IF NEEDED
                    if (x == maxTilesX && z == maxTilesZ) {
                        // CUT OFF RIGHT AND BOTTOM
                        tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
                        tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
                    } else if (x == maxTilesX) {
                        // CUT OFF RIGHT
                        tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
                    } else if (z == maxTilesZ) {
                        // CUT OF BOTTOM
                        tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
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
        float timePerTile = (float) duration / (float) tileCount;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Tilecreation finished in " + duration + "ms. (Aprox. " + timePerTile + "ms per tile)"), 1);
    }

    // /////////////////////////////////
    //
    // CREATE SINGLE TILE
    //
    // /////////////////////////////////
    public void createSingleTile(BufferedImage image, final int tileX, final int tileZ, final int zoomLevel) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Creating single tile : " + tileX + " / " + tileZ), 1);

        maxTilesX = (int) (image.getWidth() / TILE_SIZE);
        maxTilesZ = (int) (image.getHeight() / TILE_SIZE);

        cutRight = ((maxTilesX + 1) * TILE_SIZE) - image.getWidth();
        cutBottom = ((maxTilesZ + 1) * TILE_SIZE) - image.getHeight();

        // 8 / 32 = 0.25
        double SmallBigRatio = (double) ZOOM_TEXTURE_SIZE[zoomLevel] / (double) ZOOM_TEXTURE_SIZE[0];
        
        int tempX = (int) ((double)tileX * (double)SmallBigRatio);
        int tempZ = (int) ((double)tileZ * (double)SmallBigRatio);
        
        if(zoomLevel == 1)
            tempX++;

        BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics tileGraphic = tileImage.getGraphics();
        File f;

        tileGraphic.setColor(Color.BLACK);
        long startTime = System.currentTimeMillis();

        try {
            // DRAW IMAGE
            tileGraphic.drawImage(image, -(int)(tempX * TILE_SIZE), -(int)(tempZ * TILE_SIZE), null);

            // CREATE TILE-FILE
            f = new File("plugins/BuyCraft/markets/tiles/" + realZoomLevels[zoomLevel] + "_" + tempX + "_" + tempZ + ".png");
            if (f.exists())
                f.delete();
            f.createNewFile();

            // REPAINT THE SIDES, IF NEEDED
            if (tileX == maxTilesX && tileZ == maxTilesZ) {
                // CUT OFF RIGHT AND BOTTOM
                tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
                tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
            } else if (tileX == maxTilesX) {
                // CUT OFF RIGHT
                tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
            } else if (tileZ == maxTilesZ) {
                // CUT OF BOTTOM
                tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
            }
            // SAVE FILE TO HDD
            ImageIO.write(tileImage, "png", f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - startTime;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Single tile finished in " + duration + "ms."), 1);
    }

    // ///////////////////////////////////////////
    //
    // METHOD 1 - ITERATE THROUGH ALL BLOCKS
    //
    // ///////////////////////////////////////////
    public void renderWithMethod1(BufferedImage image, int zoomLevel) {
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
                this.drawBlock(graphic, textureX, textureZ, image.getWidth(), image.getHeight(), TypeID, SubID, 0);
                // DRAW QUEUED BLOCKS
                for (int i = queuedBlocks.size() - 1; i >= 0; i--) {
                    this.drawBlock(graphic, textureX, textureZ, image.getWidth(), image.getHeight(), queuedBlocks.get(i).getID(), queuedBlocks.get(i).getSubID(), 0);
                }
                queuedBlocks.clear();
                textureZ += ZOOM_TEXTURE_SIZE[zoomLevel];
            }
            textureX += ZOOM_TEXTURE_SIZE[zoomLevel];
        }
    }

    // ///////////////////////////////////////////
    //
    // METHOD 2 - ITERATE THROUGH CHUNKLIST
    //
    // ///////////////////////////////////////////
    public void renderWithMethod2(BufferedImage image, int zoomLevel) {
        Graphics2D graphic = (Graphics2D) image.getGraphics();
        int minChunkX = market.getCorner1().getBlock().getChunk().getX();
        int minChunkZ = market.getCorner1().getBlock().getChunk().getZ();
        int maxChunkX = market.getCorner2().getBlock().getChunk().getX();
        int maxChunkZ = market.getCorner2().getBlock().getChunk().getZ();

        int startPosX, startPosZ;
        int offsetBlockX = market.getCorner1().getBlockX() & 0xF;
        int offsetBlockZ = market.getCorner1().getBlockZ() & 0xF;
        startPosX = 0 - (offsetBlockX * ZOOM_TEXTURE_SIZE[zoomLevel]);
        startPosZ = 0 - (offsetBlockZ * ZOOM_TEXTURE_SIZE[zoomLevel]);
        int chunkSize = 16 * ZOOM_TEXTURE_SIZE[zoomLevel];
        for (int x = minChunkX; x <= maxChunkX; x++) {
            startPosZ = 0 - (offsetBlockZ * ZOOM_TEXTURE_SIZE[zoomLevel]);
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                this.drawCompleteChunk(graphic, snapList.get(x + "_" + z), startPosX, startPosZ, image.getWidth(), image.getHeight(), zoomLevel);
                startPosZ += chunkSize;
            }
            startPosX += chunkSize;
        }
    }

    public void renderSpecificRegionWithMethod2(BufferedImage image, Point minChunk, Point maxChunk, int zoomLevel) {
        Graphics2D graphic = (Graphics2D) image.getGraphics();
        int minChunkX = market.getCorner1().getBlock().getChunk().getX();
        int minChunkZ = market.getCorner1().getBlock().getChunk().getZ();
        int maxChunkX = market.getCorner2().getBlock().getChunk().getX();
        int maxChunkZ = market.getCorner2().getBlock().getChunk().getZ();

        int startPosX, startPosZ;
        int offsetBlockX = market.getCorner1().getBlockX() & 0xF;
        int offsetBlockZ = market.getCorner1().getBlockZ() & 0xF;
        startPosX = 0 - (offsetBlockX * ZOOM_TEXTURE_SIZE[zoomLevel]);
        startPosZ = 0 - (offsetBlockZ * ZOOM_TEXTURE_SIZE[zoomLevel]);
        int chunkSize = 16 * ZOOM_TEXTURE_SIZE[zoomLevel];
        int iX = 0;
        int iZ = 0;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            startPosZ = 0 - (offsetBlockZ * ZOOM_TEXTURE_SIZE[zoomLevel]);
            iZ = 0;
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                if (iX >= minChunk.x && iX <= maxChunk.x && iZ >= minChunk.y && iZ <= maxChunk.y) {
                    this.drawCompleteChunk(graphic, snapList.get(x + "_" + z), startPosX, startPosZ, image.getWidth(), image.getHeight(), zoomLevel);
                }
                startPosZ += chunkSize;
                iZ++;
            }
            startPosX += chunkSize;
            iX++;
        }
    }

    // /////////////////////////////////
    //
    // DRAW COMPLETE CHUNK
    //
    // /////////////////////////////////
    private void drawCompleteChunk(Graphics2D graphic, ChunkSnapshot snapshot, final int startPosX, final int startPosZ, final int maxWidth, final int maxHeight, final int zoomLevel) {
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
                this.drawBlock(graphic, textureX, textureZ, maxWidth, maxHeight, TypeID, SubID, 0);
                // DRAW QUEUED BLOCKS
                for (int i = queuedBlocks.size() - 1; i >= 0; i--) {
                    this.drawBlock(graphic, textureX, textureZ, maxWidth, maxHeight, queuedBlocks.get(i).getID(), queuedBlocks.get(i).getSubID(), 0);
                }
                queuedBlocks.clear();
                textureZ += ZOOM_TEXTURE_SIZE[zoomLevel];
            }
            textureX += ZOOM_TEXTURE_SIZE[zoomLevel];
        }
    }

    // /////////////////////////////////
    //
    // EXPORT HTML-PAGE
    //
    // /////////////////////////////////
    private void exportMarketHtmlPage(MarketArea area, int zoomLevel) {
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

                    areaBuilder.append(shop.getHTML_Area(thisID, relX, relZ, ZOOM_TEXTURE_SIZE[zoomLevel]));
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
    private void drawBlock(Graphics2D graphic, final int x, final int z, final int maxWidth, final int maxHeight, final int TypeID, final int SubID, final int zoomLevel) {
        if (x < 0 || z < 0 || x > maxWidth || z > maxHeight)
            return;

        String picture = "" + TypeID;
        if (TypeID == 17 || TypeID == 35 || TypeID == 43 || TypeID == 44 || TypeID == 50 || TypeID == 63 || TypeID == 65 || TypeID == 66 || TypeID == 68 || TypeID == 75 || TypeID == 76 || TypeID == 98) {
            picture += "-" + SubID;
        }

        BufferedImage blockTex = imageList.get(zoomLevel).get(picture);
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
        imageList = new HashMap<Integer, HashMap<String, BufferedImage>>();
        HashMap<String, BufferedImage> imageList_0 = new HashMap<String, BufferedImage>();
        HashMap<String, BufferedImage> imageList_1 = new HashMap<String, BufferedImage>();
        HashMap<String, BufferedImage> imageList_2 = new HashMap<String, BufferedImage>();
        HashMap<String, BufferedImage> imageList_3 = new HashMap<String, BufferedImage>();

        imageList.put(0, imageList_0);
        imageList.put(1, imageList_1);
        imageList.put(2, imageList_2);
        imageList.put(3, imageList_3);

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
                    image = resize(image, ZOOM_TEXTURE_SIZE[0], ZOOM_TEXTURE_SIZE[0]);
                    imageList_0.put(file.getCanonicalFile().getName().replace(".png", ""), image);

                    image = resize(image, ZOOM_TEXTURE_SIZE[1], ZOOM_TEXTURE_SIZE[1]);
                    imageList_1.put(file.getCanonicalFile().getName().replace(".png", ""), image);

                    image = resize(image, ZOOM_TEXTURE_SIZE[2], ZOOM_TEXTURE_SIZE[2]);
                    imageList_2.put(file.getCanonicalFile().getName().replace(".png", ""), image);

                    image = resize(image, ZOOM_TEXTURE_SIZE[3], ZOOM_TEXTURE_SIZE[3]);
                    imageList_3.put(file.getCanonicalFile().getName().replace(".png", ""), image);
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
