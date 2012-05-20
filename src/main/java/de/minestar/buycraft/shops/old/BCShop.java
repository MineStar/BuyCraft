package de.minestar.buycraft.shops.old;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BCShop implements Serializable {
    private static final long serialVersionUID = 6914889744245794196L;
    private String worldName = "";
    private int x, y, z;

    // /////////////////////////////////
    //
    // CONSTRUCTORS
    //
    // /////////////////////////////////
    public BCShop() {
    }

    public BCShop(String worldName, int x, int y, int z) {
        setWorldName(worldName);
        setX(x);
        setY(y);
        setZ(z);
    }

    // /////////////////////////////////
    //
    // METHODS FOR ALL SHOPS
    //
    // /////////////////////////////////

    public static int countItemInInventory(Inventory inv, int ItemID, short SubData) {
        int count = 0;

        for (ItemStack item : inv.getContents()) {
            if (item == null)
                continue;

            if (item.getTypeId() == ItemID && item.getDurability() == SubData) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static int countItemInInventory(Inventory inv, int ItemID) {
        return countItemInInventory(inv, ItemID, (short) 0);
    }

    public static boolean checkCreation(String[] lines) {
        try {
            String line1 = getSpecialTextOnLine(lines[1], "{", "}");
            String[] split = line1.split(":");
            if (split.length > 1) {
                Integer.valueOf(split[1]);
            }

            String[] myLines = lines.clone();
            if (myLines[2].length() < 1)
                myLines[2] = "0:0";

            if (myLines[3].length() < 1)
                myLines[3] = myLines[2];

            String[] line3 = myLines[2].split(":");
            String[] line4 = myLines[3].split(":");

            if (Integer.valueOf(line3[0]) > 0 || Integer.valueOf(line3[0]) > 0) {
                if (Integer.valueOf(line3[0]) < 1)
                    return false;
                if (Integer.valueOf(line3[1]) < 1)
                    return false;
            }

            if (Integer.valueOf(line4[0]) > 0 || Integer.valueOf(line4[0]) > 0) {
                if (Integer.valueOf(line4[0]) < 1)
                    return false;
                if (Integer.valueOf(line4[1]) < 1)
                    return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String[] getSplit(String line) {
        String[] split = new String[2];
        split[0] = "";
        split[1] = "0";
        try {
            String[] data = line.split(":");
            split[0] = data[0];
            if (data.length > 1)
                split[1] = data[1];

            return split;
        } catch (Exception e) {
            return split;
        }
    }

    public static Integer[] getRatios(String line) {
        Integer[] ratios = new Integer[2];
        try {
            String[] split = line.split(":");
            ratios[0] = Integer.valueOf(split[0]);
            ratios[1] = Integer.valueOf(split[1]);
        } catch (Exception e) {
            ratios[0] = 1;
            ratios[1] = 1;
        }
        return ratios;
    }

    public static String getItemName(String str) {
        String tmp = getSpecialTextOnLine(str, "{", "}");
        String[] split = tmp.split(":");
        if (split.length == 1)
            return split[0];
        else
            return "";
    }

    public static short getItemData(String str) {
        try {
            String tmp = getSpecialTextOnLine(str, "{", "}");
            String[] split = tmp.split(":");
            if (split.length > 1)
                return Short.valueOf(split[1]);
            else
                return (short) 0;
        } catch (Exception e) {
            return (short) 0;
        }
    }

    public static String getSpecialTextOnLine(String str, String prefix, String suffix) {
        String signText = str;

        if (signText == null) {
            return "";
        }

        signText = signText.trim();

        if (signText.startsWith(prefix) && signText.endsWith(suffix) && signText.length() > 2) {
            String text = signText.substring(1, signText.length() - 1);
            text = text.trim();
            if (text.equals("")) {
                return "";
            }
            return text;
        }
        return "";
    }

    public void handleRightClick(Player player, Sign sign, Chest chest) {
    }

    public void handleLeftClick(Player player, Sign sign, Chest chest) {
    }

    public Block getBlock() {
        World world = Bukkit.getWorld(getWorldName());
        if (world == null)
            return null;

        return world.getBlockAt(getX(), getY(), getZ());
    }

    @Override
    public String toString() {
        return getWorldName() + "__" + getX() + "_" + getY() + "_" + getZ();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BCShop))
            return false;

        return toString().equalsIgnoreCase(((BCShop) obj).toString());
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
