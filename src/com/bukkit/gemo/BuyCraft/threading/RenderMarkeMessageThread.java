package com.bukkit.gemo.BuyCraft.threading;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RenderMarkeMessageThread implements Runnable {    
    private final String playerName;
    private final String text;
    
    public RenderMarkeMessageThread(final String playerName, final String text) {
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
