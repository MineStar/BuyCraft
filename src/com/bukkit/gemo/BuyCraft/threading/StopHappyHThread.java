package com.bukkit.gemo.BuyCraft.threading;

import java.util.TimerTask;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.bukkit.gemo.BuyCraft.BCCore;

public class StopHappyHThread extends TimerTask {

    private Server server;

    public StopHappyHThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        BCCore.happyHourItem = -1;
        String text = "Stullen Andi : Die Happy Hour ist vorbei!";
        for (Player p : server.getOnlinePlayers())
            p.sendMessage(text);
    }

}
