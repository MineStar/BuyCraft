package com.bukkit.gemo.BuyCraft;

import org.bukkit.Location;

public class MarketSelection {
    private Location corner1 = null, corner2 = null;

    public Location getCorner1() {
        return corner1;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
    }

    public boolean isValid() {
        return this.corner1 != null && this.corner2 != null;
    }
}
