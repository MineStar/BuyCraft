package de.minestar.buycraft.units;

import org.bukkit.Location;

public class BlockVector implements Comparable<BlockVector> {
    private final int x, y, z;
    private String worldName;
    private int hashCode = Integer.MIN_VALUE;

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
    public BlockVector(String worldName, int x, int y, int z) {
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
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
     * @return the worldName
     */
    public String getWorldName() {
        return worldName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof BlockVector) {
            return this.equals((BlockVector) obj);
        }

        return false;
    }

    /**
     * Create a BlockVector from a given string. The string must have the same syntax as <code>@toString()</code>
     * 
     * @param string
     * @return the BlockVector for this string, or null if it fails
     */
    public static BlockVector fromString(String string) {
        BlockVector vector = null;
        try {
            string = string.replace(" ", "").replace("BlockVector={", "").replace("}", "");
            String[] split = string.split(";");
            vector = new BlockVector(split[0], Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
        } catch (Exception e) {
            vector = null;
        }
        return vector;
    }

    /**
     * Returns a new BlockVector that is relative to this BlockVector with the given positions
     * 
     * @param x
     * @param y
     * @param z
     * @return the relative BlockVector
     */
    public BlockVector getRelative(int x, int y, int z) {
        return new BlockVector(this.worldName, this.x + x, this.y + y, this.z + z);
    }

    /**
     * Check if another BlockVector equals this BlockVector
     * 
     * @param other
     * @return <b>true</b> if the vectors are equal, otherwise <b>false</b>
     */
    public boolean equals(BlockVector other) {
        return (this.x == other.x && this.y == other.y && this.z == other.z && this.worldName.equalsIgnoreCase(other.worldName));
    }

    @Override
    public int hashCode() {
        if (hashCode == Integer.MIN_VALUE) {
            this.hashCode = this.toString().hashCode();
        }
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "BlockVector={ " + this.worldName + " ; " + this.x + " ; " + this.y + " ; " + this.z + " }";
    }

    @Override
    public int compareTo(BlockVector other) {
        return this.hashCode() - other.hashCode();
    }
}
