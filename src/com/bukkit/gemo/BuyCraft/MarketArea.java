package com.bukkit.gemo.BuyCraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class MarketArea {
    private Location corner1 = null, corner2 = null;
    private String areaName = null;

    public MarketArea(String areaName, Location loc1, Location loc2) {
        this.areaName = areaName;
        this.corner1 = loc1.clone();
        this.corner2 = loc2.clone();
        this.corner1.setX(Math.min(loc1.getX(), loc2.getX()));
        this.corner2.setX(Math.max(loc1.getX(), loc2.getX()));
        this.corner1.setY(Math.min(loc1.getY(), loc2.getY()));
        this.corner2.setY(Math.max(loc1.getY(), loc2.getY()));
        this.corner1.setZ(Math.min(loc1.getZ(), loc2.getZ()));
        this.corner2.setZ(Math.max(loc1.getZ(), loc2.getZ()));
    }

    public MarketArea(String str) {
        str = str.replace(",", "");
        String[] split = str.split("#");
        if (split.length != 3) {
            return;
        }
        setAreaName(split[0]);
        setCorner1(getLocationFromString(split[1]));
        setCorner2(getLocationFromString(split[2]));
    }

    private Location getLocationFromString(String str) {
        String[] split = str.split(";");
        if (split.length != 4) {
            return null;
        }

        World world = Bukkit.getServer().getWorld(split[0]);
        if (world == null)
            return null;

        try {
            double x = Double.valueOf(split[1]);
            double y = Double.valueOf(split[2]);
            double z = Double.valueOf(split[3]);
            return new Location(world, x, y, z, 0f, 0f);
        } catch (Exception e) {
            return null;
        }
    }

    public String exportArea() {
        return areaName + "#" + exportLocation(corner1) + "#" + exportLocation(corner2);
    }

    private String exportLocation(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ();
    }

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

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public boolean isValidArea() {
        return corner1 != null && corner2 != null && areaName != null;
    }

    public int getAreaBlockWidth() {
        return Math.abs(corner2.getBlockX() - corner1.getBlockX()) + 1;
    }

    public int getAreaBlockLength() {
        return Math.abs(corner2.getBlockZ() - corner1.getBlockZ()) + 1;
    }

    public int getAreaBlockHeight() {
        return Math.abs(corner2.getBlockY() - corner1.getBlockY()) + 1;
    }
    
    public boolean isBlockInArea(Location other) {
        if(other.getBlockX() < corner1.getBlockX() || other.getBlockX() > corner2.getBlockX())
            return false;
        
        if(other.getBlockZ() < corner1.getBlockZ() || other.getBlockZ() > corner2.getBlockZ())
            return false;
        
        if(other.getBlockY() < corner1.getBlockY() || other.getBlockY() > corner2.getBlockY())
            return false;
        
        return true;
    }

    public MarketArea clone() {
        return new MarketArea(getAreaName(), corner1, corner2);
    }
    
    
}
