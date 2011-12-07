package com.bukkit.gemo.BuyCraft.threading;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

import com.bukkit.gemo.BuyCraft.BCBlockListener;
import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.BCUserShop;
import com.bukkit.gemo.BuyCraft.MarketArea;

public class QueuedMarketRendererUpdatePageThread implements Runnable {

    private ArrayList<String> filesToUpdate;

    public QueuedMarketRendererUpdatePageThread() {
        filesToUpdate = new ArrayList<String>();
    }

    public void addMarket(BCUserShop shop) {
        synchronized (filesToUpdate) {
            String marketName = null;
            for (Map.Entry<String, MarketArea> market : BCCore.getMarketList().entrySet()) {
                if (market.getValue().isBlockInArea(shop.getSign().getBlock().getLocation())) {
                    marketName = market.getValue().getAreaName();
                    break;
                }
            }
            if (marketName == null)
                return;

            for (String entry : filesToUpdate) {
                if (marketName.equalsIgnoreCase(entry)) {
                    return;
                }
            }
            filesToUpdate.add(marketName);
        }
    }
    @Override
    public void run() {
        synchronized (filesToUpdate) {
            //if(filesToUpdate.size() > 0)
            //    Bukkit.getServer().broadcastMessage("rendering pages... " + filesToUpdate.size());
           
            for (String entry : filesToUpdate) {
                // CREATE SNAPSHOT-LIST
                MarketArea thisArea = BCCore.getMarketList().get(entry);
                if (thisArea == null)
                    continue;

                Location loc1 = thisArea.getCorner1();
                Location loc2 = thisArea.getCorner2();
                TreeMap<String, ChunkSnapshot> chunkList = new TreeMap<String, ChunkSnapshot>();
                int minChunkX = loc1.getBlock().getChunk().getX();
                int maxChunkX = loc2.getBlock().getChunk().getX();
                int minChunkZ = loc1.getBlock().getChunk().getZ();
                int maxChunkZ = loc2.getBlock().getChunk().getZ();
                World world = loc1.getWorld();
                ChunkSnapshot snap = null;
                for (int x = minChunkX; x <= maxChunkX; x++) {
                    for (int z = minChunkZ; z <= maxChunkZ; z++) {
                        snap = world.getChunkAt(x, z).getChunkSnapshot(true, true, true);
                        if (snap != null) {
                            chunkList.put(x + "_" + z, snap);
                        }
                    }
                }
                
                RenderMarketThread updateThread = new RenderMarketThread(null, chunkList, BCBlockListener.userShopList, thisArea, true);
                Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(BCCore.getPlugin(), updateThread, 1);
            }
            filesToUpdate.clear();
        }
    }

}
