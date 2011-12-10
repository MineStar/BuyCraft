package com.bukkit.gemo.BuyCraft.threading;

import java.awt.Point;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

import com.bukkit.gemo.BuyCraft.BCBlockListener;
import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.MarketArea;

public class QueuedMarketRendererUpdateTileThread implements Runnable {

    private TreeMap<String, QueuedMarketTile> tilesToUpdate;

    public QueuedMarketRendererUpdateTileThread() {
        tilesToUpdate = new TreeMap<String, QueuedMarketTile>();
    }

    public void addMarket(String marketName, Location loc) {
        synchronized (tilesToUpdate) {
            Point newTiles[] = new Point[RenderMarketThread.getZOOM_TEXTURE_SIZE().length];
            for (int i = 0; i < newTiles.length; i++) {
                newTiles[i] = RenderMarketThread.getTilePosition(loc, BCCore.getMarketList().get(marketName), i);
            }

            for (int i = 0; i < newTiles.length; i++) {
                if (!tilesToUpdate.containsKey(marketName + "_" + i + "_" + newTiles[i].x + "_" + newTiles[i].y)) {
                    tilesToUpdate.put(marketName + "_" + i + "_" + newTiles[i].x + "_" + newTiles[i].y, new QueuedMarketTile(marketName, loc.clone(), i));
                }
            }
        }
    }

    @Override
    public void run() {
        synchronized (tilesToUpdate) {
            TreeMap<String, ChunkSnapshot> chunkList = new TreeMap<String, ChunkSnapshot>();
            String oldMarketName = "";

            for (Map.Entry<String, QueuedMarketTile> entry : tilesToUpdate.entrySet()) {
                // CREATE SNAPSHOT-LIST
                MarketArea thisArea = BCCore.getMarketList().get(entry.getValue().getMarketName());
                if (thisArea == null)
                    continue;

                if (!oldMarketName.equalsIgnoreCase(thisArea.getAreaName())) {
                    oldMarketName = thisArea.getAreaName();
                    chunkList.clear();
                    int minChunkX = thisArea.getCorner1().getBlock().getChunk().getX();
                    int maxChunkX = thisArea.getCorner2().getBlock().getChunk().getX();
                    int minChunkZ = thisArea.getCorner1().getBlock().getChunk().getZ();
                    int maxChunkZ = thisArea.getCorner2().getBlock().getChunk().getZ();
                    World world = thisArea.getCorner1().getWorld();
                    ChunkSnapshot snap = null;
                    for (int x = minChunkX; x <= maxChunkX; x++) {
                        for (int z = minChunkZ; z <= maxChunkZ; z++) {
                            snap = world.getChunkAt(x, z).getChunkSnapshot(true, true, true);
                            if (snap != null) {
                                chunkList.put(x + "_" + z, snap);
                            }
                        }
                    }
                }
                RenderMarketThread updateThread = new RenderMarketThread(null, entry.getValue().getChangedLocation(), chunkList, BCBlockListener.userShopList, thisArea, entry.getValue().getZoomLevel());
                Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(BCCore.getPlugin(), updateThread, 1);
            }
            tilesToUpdate.clear();
        }
    }

}
