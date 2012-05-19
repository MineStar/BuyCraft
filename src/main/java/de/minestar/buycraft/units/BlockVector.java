package de.minestar.buycraft.units;

import org.bukkit.Location;

public class BlockVector {
    private int x, y, z;
    private String worldName;

    /**
     * Constructor
     * 
     * @param the
     *            x
     * @param the
     *            y
     * @param the
     *            z
     */
    public BlockVector(int x, int y, int z, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    /**
     * Constructor
     * 
     * @param the
     *            location
     */
    public BlockVector(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @return the z
     */
    public int getZ() {
        return z;
    }

    /**
     * @param x
     *            the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @param y
     *            the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @param z
     *            the z to set
     */
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * @return the worldName
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * @param worldName
     *            the worldName to set
     */
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public boolean equals(BlockVector other) {
        return (this.x == other.x && this.y == other.y && this.z == other.z && this.worldName.equalsIgnoreCase(other.worldName));
    }

    @Override
    public String toString() {
        return "BlockVector={ " + this.worldName + " ; " + this.x + " ; " + this.y + " ; " + this.z + " }";
    }
}
