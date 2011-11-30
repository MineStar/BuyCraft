package com.bukkit.gemo.BuyCraft.threading;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RenderMarketFinishedThread implements Runnable {    
    private String playerName;
    private String text;
    
    public RenderMarketFinishedThread(final String playerName, final String text) {
        this.playerName = playerName;
        this.text = text;
    }
    
    @Override
    public void run() {
        Player player = Bukkit.getServer().getPlayer(playerName);
        if(player != null)
            player.sendMessage(text);
    }
}
