package com.bukkit.gemo.BuyCraft.threading;

import java.awt.Point;
import java.util.ArrayList;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

import com.bukkit.gemo.BuyCraft.BCBlockListener;
import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.MarketArea;

public class QueuedMarketRendererUpdateTileThread implements Runnable {

    private ArrayList<QueuedMarketTile> tilesToUpdate;

    public QueuedMarketRendererUpdateTileThread() {
        tilesToUpdate = new ArrayList<QueuedMarketTile>();
    }

    public void addMarket(String marketName, Location loc) {
        synchronized (tilesToUpdate) {
            Point newTile = RenderMarketThread.getTilePosition(loc, BCCore.getMarketList().get(marketName));
            Point oldTile;
            for (QueuedMarketTile entry : tilesToUpdate) {
                oldTile = RenderMarketThread.getTilePosition(entry.getChangedLocation(), BCCore.getMarketList().get(entry.getMarketName()));
                if (!marketName.equalsIgnoreCase(entry.getMarketName())) {
                    continue;
                }
                if (oldTile.x == newTile.x && oldTile.y == newTile.y) {
                    return;
                }
            }
            tilesToUpdate.add(new QueuedMarketTile(marketName, loc.clone()));
        }
    }

    @Override
    public void run() {
        synchronized (tilesToUpdate) {
            // if(tilesToUpdate.size() > 0)
            // Bukkit.getServer().broadcastMessage("rendering tiles... " +
            // tilesToUpdate.size());

            for (QueuedMarketTile entry : tilesToUpdate) {
                // CREATE SNAPSHOT-LIST
                MarketArea thisArea = BCCore.getMarketList().get(entry.getMarketName());
                if (thisArea == null)
                    continue;

                TreeMap<String, ChunkSnapshot> chunkList = new TreeMap<String, ChunkSnapshot>();
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
                RenderMarketThread updateThread = new RenderMarketThread(null, entry.getChangedLocation(), chunkList, BCBlockListener.userShopList, thisArea, false);
                Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(BCCore.getPlugin(), updateThread, 1);
            }
            tilesToUpdate.clear();
        }
    }

}
