package com.bukkit.gemo.BuyCraft.threading;

import org.bukkit.Location;

public class QueuedMarketTile {
    private final String marketName;
    private final Location changedLocation;
    
    public QueuedMarketTile(String marketName, Location changedLocation) {
        this.marketName = marketName;
        this.changedLocation = changedLocation;
    }

    public String getMarketName() {
        return marketName;
    }

    public Location getChangedLocation() {
        return changedLocation;
    }       
}
