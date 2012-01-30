package com.bukkit.gemo.BuyCraft.threading;

import org.bukkit.Location;

public class QueuedMarketTile {
    private final String marketName;
    private final Location changedLocation;
    private final int zoomLevel;

    public QueuedMarketTile(String marketName, Location changedLocation, int zoomLevel) {
        this.marketName = marketName;
        this.changedLocation = changedLocation;
        this.zoomLevel = zoomLevel;
    }

    public String getMarketName() {
        return marketName;
    }

    public Location getChangedLocation() {
        return changedLocation;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

}
