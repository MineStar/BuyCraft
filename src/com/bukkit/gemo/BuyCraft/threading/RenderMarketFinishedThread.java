package com.bukkit.gemo.BuyCraft.threading;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RenderMarketFinishedThread implements Runnable {
    
    private long duration;
    private String playerName;
    private String marketName;
    
    public RenderMarketFinishedThread(long duration, final String playerName, final String marketName) {
        this.duration = duration;
        this.playerName = playerName;
        this.marketName = marketName;
    }
    
    @Override
    public void run() {
        Player player = Bukkit.getServer().getPlayer(playerName);
        if(player != null)
            player.sendMessage(ChatColor.GREEN + "Rendering of '" + this.marketName + "' finished in " + duration + "ms.");
    }
}
