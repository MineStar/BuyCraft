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
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;

import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.BCMinimalItem;
import com.bukkit.gemo.BuyCraft.BCUserShop;
import com.bukkit.gemo.BuyCraft.MarketArea;

public class RenderMarketThread implements Runnable {

    private static int[] ZOOM_TEXTURE_SIZE = {32, 16, 8, 4, 2, 1};
    private TreeMap<String, ChunkSnapshot> snapList;
    private HashMap<String, BCUserShop> userShopList;
    private static HashMap<Integer, HashMap<String, BufferedImage>> imageList;
    private MarketArea market;
    private String playerName;
    private boolean renderAll = true;
    private Location changedLocation = null;
    private int[] realZoomLevels;
    private final String marketDir;
    private final String marketTileDir;
    private boolean exportPageOnly;

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
        realZoomLevels = new int[ZOOM_TEXTURE_SIZE.length];
        for (int i = 0; i < realZoomLevels.length; i++) {
            realZoomLevels[i] = realZoomLevels.length - (i + 1);
        }

        // this.marketDir = "/var/www/vhosts/minestar.de/httpdocs/usershops/" +
        // market.getAreaName() + "/";
       //this.marketDir = "plugins/BuyCraft/markets/" + market.getAreaName() + "/";

        this.marketDir = BCCore.getHttpPath() + market.getAreaName() + "/";
        
        this.marketTileDir = marketDir + "tiles/";
        // CREATE NEEDED DIRS
        new File(marketTileDir).mkdirs();
        this.exportPageOnly = false;
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

    // ////////////////////////////////////////////
    //
    // CONSTRUCTOR FOR PAGE-CREATION ONLY
    //
    // ////////////////////////////////////////////
    public RenderMarketThread(final String playerName, final TreeMap<String, ChunkSnapshot> chunkList, final HashMap<String, BCUserShop> userShopList, MarketArea market, boolean exportPageOnly) {
        this(playerName, chunkList, userShopList, market);
        this.exportPageOnly = exportPageOnly;
    }

    // /////////////////////////////////
    //
    // RENDERING THREAD
    //
    // /////////////////////////////////
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        if (!exportPageOnly) {
            try {
                // CREATE IMAGE
                BufferedImage image = new BufferedImage(market.getAreaBlockWidth() * ZOOM_TEXTURE_SIZE[0], market.getAreaBlockLength() * ZOOM_TEXTURE_SIZE[0], BufferedImage.TYPE_INT_RGB);

                File output = new File(marketDir + "bigImage.png");
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
                    for (int i = 0; i < ZOOM_TEXTURE_SIZE.length; i++) {
                        BufferedImage img = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[i] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[i] * market.getAreaBlockLength());
                        createAllTiles_OSM(img, i);
                        img.flush();
                        img = null;
                    }

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
                    int extraZ = image.getHeight() % TILE_SIZE;                  
                    int distX = changedLocation.getBlockX() - market.getCorner1().getBlockX();
                    int distZ = changedLocation.getBlockZ() - market.getCorner1().getBlockZ();
                    singleTileX = (int) (distX * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);
                    singleTileZ = (int) (distZ * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);

                    int nextSingleTileX = (int) ((distX + 1) * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);
                    int nextSingleTileZ = (int) ((distZ + 1) * ZOOM_TEXTURE_SIZE[0] / TILE_SIZE);

                    System.out.println("extraZ: " + extraZ);
                    System.out.println("distZ: " + distZ);
                    System.out.println("Tile: " + singleTileX + " / " + singleTileZ);
                    
                    /*
                     * // 8 / 32 = 0.25 double SmallBigRatio = (double)
                     * ZOOM_TEXTURE_SIZE[3] / (double) ZOOM_TEXTURE_SIZE[0]; //
                     * 32 / 8 = 4 int BigSmallRatio = (int) ZOOM_TEXTURE_SIZE[0]
                     * / ZOOM_TEXTURE_SIZE[3];
                     * 
                     * double tempX = (double) singleTileX * SmallBigRatio;
                     * double tempZ = (double) singleTileZ * SmallBigRatio;
                     * 
                     * int minChunkX = ((int) tempX) * BigSmallRatio; int
                     * minChunkZ = ((int) tempZ) * BigSmallRatio; int maxChunkX
                     * = minChunkX + BigSmallRatio; int maxChunkZ = minChunkZ +
                     * BigSmallRatio; minChunkX--; minChunkZ--;
                     */

                    if (singleTileX < 0 || singleTileZ < 0 || singleTileX > maxTilesX || singleTileZ > maxTilesZ) {
                        return;
                    }

                    this.renderWithMethod2(image, 0);
                    // this.renderSpecificRegionWithMethod2(image, new
                    // Point(minChunkX, minChunkZ), new Point(maxChunkX,
                    // maxChunkZ),
                    // 0);

                    // CREATE TILES
                    for (int i = 0; i < ZOOM_TEXTURE_SIZE.length; i++) {
                        BufferedImage img = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[i] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[i] * market.getAreaBlockLength());
                        createSingleTile_OSM(img, singleTileX, singleTileZ, i);
                        img.flush();
                        img = null;
                    }

                    // BLOCK ON TILE-BORDER = CREATE MORE TILES
                    if (nextSingleTileX != singleTileX || nextSingleTileZ != singleTileZ) {
                        if (nextSingleTileX <= maxTilesX || nextSingleTileZ <= maxTilesZ) {
                            // CREATE TILES
                            for (int i = 0; i < ZOOM_TEXTURE_SIZE.length; i++) {
                                BufferedImage img = RenderMarketThread.resize(image, ZOOM_TEXTURE_SIZE[i] * market.getAreaBlockWidth(), ZOOM_TEXTURE_SIZE[i] * market.getAreaBlockLength());
                                createSingleTile_OSM(img, nextSingleTileX, nextSingleTileZ, i);
                                img.flush();
                                img = null;
                            }
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
    private void createAllTiles_OSM(BufferedImage image, int zoomLevel) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Creating tiles... (zoomlevel " + zoomLevel + " )"), 1);

        maxTilesX = (int) (image.getWidth() / TILE_SIZE);
        maxTilesZ = (int) (image.getHeight() / TILE_SIZE);

        cutRight = ((maxTilesX + 1) * TILE_SIZE) - image.getWidth();
        cutBottom = ((maxTilesZ + 1) * TILE_SIZE) - image.getHeight();

        BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TRANSLUCENT);
        Graphics tileGraphic = tileImage.getGraphics();
        File f;

        int tileCount = 0;
        long startTime = System.currentTimeMillis();
        for (int x = 0; x <= maxTilesX; x++) {
            for (int z = 0; z <= maxTilesZ; z++) {
                try {
                    // DRAW IMAGE
                    tileGraphic.drawImage(image, -(x * TILE_SIZE), -image.getHeight() + TILE_SIZE + (z * TILE_SIZE), null);

                    // CREATE TILE-FILE
                    f = new File(marketTileDir + realZoomLevels[zoomLevel] + "_" + x + "_" + z + ".png");
                    if (f.exists())
                        f.delete();
                    f.createNewFile();

                    // REPAINT THE SIDES, IF NEEDED
                    if (x == maxTilesX && z == maxTilesZ) {
                        // CUT OFF RIGHT AND TOP
                        tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
                        tileGraphic.fillRect(0, 0, TILE_SIZE, cutBottom);
                    } else if (x == maxTilesX) {
                        // CUT OFF RIGHT
                        tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
                    } else if (z == maxTilesZ) {
                        // CUT OF TOP
                        tileGraphic.fillRect(0, 0, TILE_SIZE, cutBottom);
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

//    private void createAllTiles_GOOGLE(BufferedImage image, int zoomLevel) {
//        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Creating tiles... (zoomlevel " + zoomLevel + " )"), 1);
//
//        maxTilesX = (int) (image.getWidth() / TILE_SIZE);
//        maxTilesZ = (int) (image.getHeight() / TILE_SIZE);
//
//        cutRight = ((maxTilesX + 1) * TILE_SIZE) - image.getWidth();
//        cutBottom = ((maxTilesZ + 1) * TILE_SIZE) - image.getHeight();
//
//        BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
//        Graphics tileGraphic = tileImage.getGraphics();
//        File f;
//
//        tileGraphic.setColor(Color.BLACK);
//        int tileCount = 0;
//        long startTime = System.currentTimeMillis();
//        for (int x = 0; x <= maxTilesX; x++) {
//            for (int z = 0; z <= maxTilesZ; z++) {
//                try {
//                    // DRAW IMAGE
//                    tileGraphic.drawImage(image, -(x * TILE_SIZE), -(z * TILE_SIZE), null);
//
//                    // CREATE TILE-FILE
//                    f = new File(marketTileDir + realZoomLevels[zoomLevel] + "_" + x + "_" + z + ".png");
//                    if (f.exists())
//                        f.delete();
//                    f.createNewFile();
//
//                    // REPAINT THE SIDES, IF NEEDED
//                    if (x == maxTilesX && z == maxTilesZ) {
//                        // CUT OFF RIGHT AND BOTTOM
//                        tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
//                        tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
//                    } else if (x == maxTilesX) {
//                        // CUT OFF RIGHT
//                        tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
//                    } else if (z == maxTilesZ) {
//                        // CUT OF BOTTOM
//                        tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
//                    }
//
//                    // SAVE FILE TO HDD
//                    ImageIO.write(tileImage, "png", f);
//                    tileCount++;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        long duration = System.currentTimeMillis() - startTime;
//        float timePerTile = (float) duration / (float) tileCount;
//        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Tilecreation finished in " + duration + "ms. (Aprox. " + timePerTile + "ms per tile)"), 1);
//    }

    // /////////////////////////////////
    //
    // CREATE SINGLE TILE
    //
    // /////////////////////////////////
    private void createSingleTile_OSM(BufferedImage image, final int tileX, final int tileZ, final int zoomLevel) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Creating single tile : " + tileX + " / " + tileZ), 1);

        maxTilesX = (int) (image.getWidth() / TILE_SIZE);
        maxTilesZ = (int) (image.getHeight() / TILE_SIZE);

        cutRight = ((maxTilesX + 1) * TILE_SIZE) - image.getWidth();
        cutBottom = ((maxTilesZ + 1) * TILE_SIZE) - image.getHeight();

        // 8 / 32 = 0.25
        double SmallBigRatio = (double) ZOOM_TEXTURE_SIZE[zoomLevel] / (double) ZOOM_TEXTURE_SIZE[0];

        int tempX = (int) ((double) tileX * (double) SmallBigRatio);
        int tempZ = (int) ((double) tileZ * (double) SmallBigRatio);

        if (zoomLevel == 1) {
            tempX++;
        }

        
        
        BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics tileGraphic = tileImage.getGraphics();
        File f;

        tileGraphic.setColor(Color.BLACK);
        long startTime = System.currentTimeMillis();

        try {
            // DRAW IMAGE
            tileGraphic.drawImage(image, -(tempX * TILE_SIZE), -image.getHeight() + TILE_SIZE + (tempZ * TILE_SIZE), null);

            // tileGraphic.drawImage(image, -(int) (tempX * TILE_SIZE), -(int)
            // (tempZ * TILE_SIZE), null);

            // CREATE TILE-FILE
            f = new File(marketTileDir + realZoomLevels[zoomLevel] + "_" + tempX + "_" + tempZ + ".png");
            if (f.exists())
                f.delete();
            f.createNewFile();

            // REPAINT THE SIDES, IF NEEDED
            if (tileX == maxTilesX && tileZ == maxTilesZ) {
                // CUT OFF RIGHT AND TOP
                tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
                tileGraphic.fillRect(0, 0, TILE_SIZE, cutBottom);
            } else if (tileX == maxTilesX) {
                // CUT OFF RIGHT
                tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
            } else if (tileZ == maxTilesZ) {
                // CUT OF TOP
                tileGraphic.fillRect(0, 0, TILE_SIZE, cutBottom);
            }
            // SAVE FILE TO HDD
            ImageIO.write(tileImage, "png", f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - startTime;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Single tile finished in " + duration + "ms."), 1);
    }

//    private void createSingleTile_GOOGLE(BufferedImage image, final int tileX, final int tileZ, final int zoomLevel) {
//        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Creating single tile : " + tileX + " / " + tileZ), 1);
//
//        maxTilesX = (int) (image.getWidth() / TILE_SIZE);
//        maxTilesZ = (int) (image.getHeight() / TILE_SIZE);
//
//        cutRight = ((maxTilesX + 1) * TILE_SIZE) - image.getWidth();
//        cutBottom = ((maxTilesZ + 1) * TILE_SIZE) - image.getHeight();
//
//        // 8 / 32 = 0.25
//        double SmallBigRatio = (double) ZOOM_TEXTURE_SIZE[zoomLevel] / (double) ZOOM_TEXTURE_SIZE[0];
//
//        int tempX = (int) ((double) tileX * (double) SmallBigRatio);
//        int tempZ = (int) ((double) tileZ * (double) SmallBigRatio);
//
//        if (zoomLevel == 1)
//            tempX++;
//
//        BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
//        Graphics tileGraphic = tileImage.getGraphics();
//        File f;
//
//        tileGraphic.setColor(Color.BLACK);
//        long startTime = System.currentTimeMillis();
//
//        try {
//            // DRAW IMAGE
//            tileGraphic.drawImage(image, -(int) (tempX * TILE_SIZE), -(int) (tempZ * TILE_SIZE), null);
//
//            // CREATE TILE-FILE
//            f = new File(marketTileDir + realZoomLevels[zoomLevel] + "_" + tempX + "_" + tempZ + ".png");
//            if (f.exists())
//                f.delete();
//            f.createNewFile();
//
//            // REPAINT THE SIDES, IF NEEDED
//            if (tileX == maxTilesX && tileZ == maxTilesZ) {
//                // CUT OFF RIGHT AND BOTTOM
//                tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
//                tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
//            } else if (tileX == maxTilesX) {
//                // CUT OFF RIGHT
//                tileGraphic.fillRect(TILE_SIZE - cutRight, 0, cutRight, TILE_SIZE);
//            } else if (tileZ == maxTilesZ) {
//                // CUT OF BOTTOM
//                tileGraphic.fillRect(0, TILE_SIZE - cutBottom, TILE_SIZE, cutBottom);
//            }
//            // SAVE FILE TO HDD
//            ImageIO.write(tileImage, "png", f);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        long duration = System.currentTimeMillis() - startTime;
//        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Single tile finished in " + duration + "ms."), 1);
//    }

    // ///////////////////////////////////////////
    //
    // METHOD 1 - ITERATE THROUGH ALL BLOCKS
    //
    // ///////////////////////////////////////////
//    private void renderWithMethod1(BufferedImage image, int zoomLevel) {
//        Graphics2D graphic = (Graphics2D) image.getGraphics();
//        int blockChunkX;
//        int blockChunkZ;
//        int blockX, blockZ;
//        int highestY;
//
//        ArrayList<BCMinimalItem> queuedBlocks = new ArrayList<BCMinimalItem>();
//
//        int TypeID, SubID;
//        String chunkString = "";
//        int textureX = 0;
//        int textureZ = 0;
//        for (int x = 0; x < market.getAreaBlockWidth(); x++) {
//            blockX = (x + market.getCorner1().getBlockX()) & 0xF;
//            blockChunkX = (x + market.getCorner1().getBlockX()) >> 4;
//            textureZ = 0;
//            for (int z = 0; z < market.getAreaBlockLength(); z++) {
//                blockZ = (z + market.getCorner1().getBlockZ()) & 0xF;
//                blockChunkZ = (z + market.getCorner1().getBlockZ()) >> 4;
//                chunkString = blockChunkX + "_" + blockChunkZ;
//
//                highestY = snapList.get(chunkString).getHighestBlockYAt(blockX, blockZ);
//                if (highestY > market.getCorner2().getBlockY())
//                    highestY = market.getCorner2().getBlockY();
//
//                TypeID = snapList.get(chunkString).getBlockTypeId(blockX, highestY, blockZ);
//
//                while (isTextureTransparent(TypeID) && highestY > 0) {
//                    SubID = snapList.get(chunkString).getBlockData(blockX, highestY, blockZ);
//                    queuedBlocks.add(new BCMinimalItem(TypeID, SubID));
//                    highestY--;
//                    TypeID = snapList.get(chunkString).getBlockTypeId(blockX, highestY, blockZ);
//                }
//
//                SubID = snapList.get(chunkString).getBlockData(blockX, highestY, blockZ);
//                this.drawBlock(graphic, textureX, textureZ, image.getWidth(), image.getHeight(), TypeID, SubID, 0);
//                // DRAW QUEUED BLOCKS
//                for (int i = queuedBlocks.size() - 1; i >= 0; i--) {
//                    this.drawBlock(graphic, textureX, textureZ, image.getWidth(), image.getHeight(), queuedBlocks.get(i).getID(), queuedBlocks.get(i).getSubID(), 0);
//                }
//                queuedBlocks.clear();
//                textureZ += ZOOM_TEXTURE_SIZE[zoomLevel];
//            }
//            textureX += ZOOM_TEXTURE_SIZE[zoomLevel];
//        }
//    }

    // ///////////////////////////////////////////
    //
    // METHOD 2 - ITERATE THROUGH CHUNKLIST
    //
    // ///////////////////////////////////////////
    private void renderWithMethod2(BufferedImage image, int zoomLevel) {
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

//    private void renderSpecificRegionWithMethod2(BufferedImage image, Point minChunk, Point maxChunk, int zoomLevel) {
//        Graphics2D graphic = (Graphics2D) image.getGraphics();
//        int minChunkX = market.getCorner1().getBlock().getChunk().getX();
//        int minChunkZ = market.getCorner1().getBlock().getChunk().getZ();
//        int maxChunkX = market.getCorner2().getBlock().getChunk().getX();
//        int maxChunkZ = market.getCorner2().getBlock().getChunk().getZ();
//
//        int startPosX, startPosZ;
//        int offsetBlockX = market.getCorner1().getBlockX() & 0xF;
//        int offsetBlockZ = market.getCorner1().getBlockZ() & 0xF;
//        startPosX = 0 - (offsetBlockX * ZOOM_TEXTURE_SIZE[zoomLevel]);
//        startPosZ = 0 - (offsetBlockZ * ZOOM_TEXTURE_SIZE[zoomLevel]);
//        int chunkSize = 16 * ZOOM_TEXTURE_SIZE[zoomLevel];
//        int iX = 0;
//        int iZ = 0;
//        for (int x = minChunkX; x <= maxChunkX; x++) {
//            startPosZ = 0 - (offsetBlockZ * ZOOM_TEXTURE_SIZE[zoomLevel]);
//            iZ = 0;
//            for (int z = minChunkZ; z <= maxChunkZ; z++) {
//                if (iX >= minChunk.x && iX <= maxChunk.x && iZ >= minChunk.y && iZ <= maxChunk.y) {
//                    this.drawCompleteChunk(graphic, snapList.get(x + "_" + z), startPosX, startPosZ, image.getWidth(), image.getHeight(), zoomLevel);
//                }
//                startPosZ += chunkSize;
//                iZ++;
//            }
//            startPosX += chunkSize;
//            iX++;
//        }
//    }

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
    private void exportMarketHtmlPage(MarketArea area) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Creating page..."), 1);
        long startTime = System.currentTimeMillis();

        BufferedReader reader = null;
        ArrayList<String> lineList = new ArrayList<String>();
        try {
            if (!new File("plugins/BuyCraft/markets/template.html").exists()) {
                BCCore.printInConsole("Cannot create HTML-File! (template.html is missing)");
                return;
            }

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

        // FIND REPLACEABLE TEXTS
        ArrayList<String> wordList = new ArrayList<String>();
        wordList.add("%MARKETNAME%");
        wordList.add("%SHOPDETAILS%");
        wordList.add("%CREATEMARKER%");
        wordList.add("%CREATEPOPUP%");
        wordList.add("%MIDDLE_X%");
        wordList.add("%MIDDLE_Z%");
        wordList.add("%MATERIALS%");

        int found = 0;
        TreeMap<String, Integer> matchList = new TreeMap<String, Integer>();

        for (int i = 0; i < lineList.size(); i++) {
            for (int j = 0; j < wordList.size(); j++) {
                if (lineList.get(i).contains(wordList.get(j))) {
                    matchList.put(wordList.get(j), i);
                }
                if(found == wordList.size())
                    break;
            }
            if(found == wordList.size())
                break;
        }

        StringBuilder detailBuilder = new StringBuilder();
        StringBuilder markerBuilder = new StringBuilder();
        StringBuilder popupBuilder = new StringBuilder();
        HashMap<String, Integer> shopCount = new HashMap<String, Integer>();
        int nowID = 0;
        TreeMap<Integer, String> itemsOnMarket = new TreeMap<Integer, String>();

        for (BCUserShop shop : userShopList.values()) {
            if (shop.isActive() && shop.getSign() != null) {
                if (area.isBlockInArea(shop.getSign().getBlock().getLocation())) {
                    int relX = shop.getX() - area.getCorner1().getBlockX();
                    int relZ = shop.getZ() - area.getCorner1().getBlockZ();
                    int thisID = 1;
                    if (shopCount.containsKey(shop.getShopOwner()))
                        thisID = shopCount.get(shop.getShopOwner());

                    // ADD ITEM TO LIST OF AVAILABLE MARKETS
                    itemsOnMarket.put(shop.getItemID(), BCCore.getItemName(shop.getItemID()));

                    // CALCULATE POSITION ON MAP
                    int mapX = relX;
                    int mapZ = area.getAreaBlockLength() - relZ - 1;

                    // CREATE SHOP-DETAILS
                    detailBuilder.append(shop.getHTML_ShopDetails(thisID, nowID));
                    markerBuilder.append(shop.getHTML_Marker(nowID, mapX, mapZ));
                    popupBuilder.append(shop.getHTML_PopUp(nowID));

                    thisID++;
                    nowID++;
                    shopCount.put(shop.getShopOwner(), thisID);
                }
            }
        }

        // ITERATE THROUGH ITEMS ON MARKET
        StringBuilder itemBuilder = new StringBuilder();
        for (Map.Entry<Integer, String> entry : itemsOnMarket.entrySet()) {
            itemBuilder.append("\t\t\t\t\t\t<li><a href=\"#\" onClick=\"toggleShops('." + entry.getValue() + "');\">" + Material.getMaterial(entry.getKey()).name().replace("_", " ") + "</a></li>");
            itemBuilder.append(System.getProperty("line.separator"));
        }

        // REPLACE TEXTS
        lineList = this.replaceText(lineList, matchList, "%MARKETNAME%", area.getAreaName());
        lineList = this.replaceText(lineList, matchList, "%SHOPDETAILS%", detailBuilder.toString());
        lineList = this.replaceText(lineList, matchList, "%CREATEMARKER%", markerBuilder.toString());
        lineList = this.replaceText(lineList, matchList, "%CREATEPOPUP%", popupBuilder.toString());
        lineList = this.replaceText(lineList, matchList, "%MIDDLE_X%", String.valueOf((area.getAreaBlockWidth() / 2)));
        lineList = this.replaceText(lineList, matchList, "%MIDDLE_Z%", String.valueOf((area.getAreaBlockLength() / 2)));
        lineList = this.replaceText(lineList, matchList, "%MATERIALS%", itemBuilder.toString());

        // COLLECT GARBAGE
        detailBuilder.setLength(0);
        markerBuilder.setLength(0);
        popupBuilder.setLength(0);
        itemBuilder.setLength(0);
        itemsOnMarket.clear();

        this.savePage(marketDir + "index.html", lineList);
        long duration = System.currentTimeMillis() - startTime;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BCCore.getPlugin(), new RenderMarkeMessageThread(playerName, ChatColor.GREEN + "Writing " + duration + "ms."), 1);
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
    private boolean isTextureTransparent(final int ID) {
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
        for (int i = 0; i < ZOOM_TEXTURE_SIZE.length; i++) {
            HashMap<String, BufferedImage> imageList_zoom = new HashMap<String, BufferedImage>();
            imageList.put(i, imageList_zoom);
        }

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
                    // RENDER IMAGE IN ALL NEEDED SIZES
                    for (int i = 0; i < ZOOM_TEXTURE_SIZE.length; i++) {
                        image = resize(image, ZOOM_TEXTURE_SIZE[i], ZOOM_TEXTURE_SIZE[i]);
                        imageList.get(i).put(file.getCanonicalFile().getName().replace(".png", ""), image);
                    }
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
    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
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
    private ArrayList<String> replaceText(ArrayList<String> lines, TreeMap<String, Integer> matchList, String placeHolder, String newText) {
        String thisLine = lines.get(matchList.get(placeHolder));
        thisLine = thisLine.replace(placeHolder, newText);
        lines.set(matchList.get(placeHolder), thisLine);
        return lines;
    }
}
