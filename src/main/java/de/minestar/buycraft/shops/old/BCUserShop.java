package de.minestar.buycraft.shops.old;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class BCUserShop extends BCShop implements Serializable {
    private static final long serialVersionUID = 5197016944101717903L;
    private boolean isActive = false;
    private ArrayList<BCItemStack> shopInventory = null;
    private long creationTime, lastUsedTime = 0;
    private boolean shopFinished = true;

    // /////////////////////////////////
    //
    // CONSTRUCTORS
    //
    // /////////////////////////////////
    public BCUserShop() {
        super();
        shopInventory = new ArrayList<BCItemStack>();
        creationTime = System.currentTimeMillis();
        lastUsedTime = System.currentTimeMillis();
    }

    public BCUserShop(String worldName, int x, int y, int z) {
        super(worldName, x, y, z);
        shopInventory = new ArrayList<BCItemStack>();
        creationTime = System.currentTimeMillis();
        lastUsedTime = System.currentTimeMillis();
    }

    // /////////////////////////////////
    //
    // METHODS FOR GETTING SHOP-PROPERTIES
    //
    // /////////////////////////////////

    /**
     * getSign()
     * 
     * @return <b>The sign</b>, if the sign was found.<br/>
     *         otherwise <b>null</b>
     */
    public Sign getSign() {
        Block block = this.getBlock();
        if (block.getTypeId() != Material.WALL_SIGN.getId())
            return null;

        return ((Sign) block.getState());
    }

    /**
     * getLines()
     * 
     * @return <b>All lines</b>, if the sign was found.<br/>
     *         otherwise <b>null</b>
     */
    public String[] getLines() {
        Sign sign = getSign();
        if (sign == null)
            return null;

        return sign.getLines();
    }

    /**
     * getLine(int linenumber)
     * 
     * @param linenumber
     * @return <b>Linecontent</b>, if the sign was found.<br/>
     *         otherwise <b>null</b>
     */
    public String getLine(int linenumber) {
        Sign sign = getSign();
        if (sign == null)
            return null;

        if (linenumber < 0 || linenumber > 3)
            return null;
        return sign.getLines()[linenumber];
    }

    public String getShopOwner() {
        String line = getLine(0);
        if (line == null)
            return null;

        line = getSpecialTextOnLine(line, "$", "$");
        return line;
    }

    public int getItemID() {
        return 0;
    }

    public short getSubID() {
        String line = getLine(1);
        if (line == null)
            return 0;

        line = getSpecialTextOnLine(line, "{", "}");
        String[] split = line.split(":");
        try {
            return Short.valueOf(split[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    public Integer[] getBuyRatio() {
        String line = getLine(2);
        if (line == null)
            return null;

        return BCShop.getRatios(line);
    }

    public Integer[] getSellRatio() {
        String line = getLine(3);
        if (line == null)
            return null;

        return BCShop.getRatios(line);
    }

    // /////////////////////////////////
    //
    // HANDLE LEFTCLICK
    //
    // /////////////////////////////////
    public void handleLeftClick(Player player, Sign sign, Chest chest) {
    }
    // /////////////////////////////////
    //
    // HANDLE RIGHTCLICK
    //
    // /////////////////////////////////
    public void handleRightClick(Player player, Sign sign, Chest chest) {
    }
    // /////////////////////////////////
    //
    // METHODS FOR REAL INVENTORY
    //
    // /////////////////////////////////
    public void restoreInventory(Chest chest) {
    }

    public void updateInventory2(int itemID, short SubID, int updateAmount) {

    }

    // HAS ITEMS IN SHOPINVENTORY
    public boolean hasAmountOfItem(int itemID, short SubID, int count) {
        return countItemInShopInventory(itemID, SubID) >= count;
    }

    // COUNT ITEM IN SHOPINVENTORY
    public int countItemInShopInventory(int itemID, int SubID) {
        int count = 0;
        for (BCItemStack item : shopInventory) {
            if (item.getItem() != null) {
                if (item.getId() == itemID && item.getSubId() == SubID) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

    public int countShopInventory() {
        int count = 0;
        for (BCItemStack item : shopInventory) {
            if (item.getItem() != null) {
                if (item.getAmount() > 0)
                    count += item.getAmount();
            }
        }
        return count;
    }

    // /////////////////////////////////
    //
    // SAVE SHOP
    //
    // /////////////////////////////////
    public void saveShop() {
        File folder = new File("plugins/BuyCraft/UserShops/" + toString() + ".bcf");
        folder.mkdirs();

        if (folder.exists()) {
            folder.delete();
        }

        try {
            ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("plugins/BuyCraft/UserShops/" + toString() + ".bcf")));
            objOut.writeObject(this);
            objOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // /////////////////////////////////
    //
    // GETTER & SETTER
    //
    // /////////////////////////////////
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public boolean isShopFinished() {
        return shopFinished;
    }

    /**
     * @return the shopInventory
     */
    public ArrayList<BCItemStack> getShopInventory() {
        return shopInventory;
    }

    public void setShopFinished(boolean shopFinished) {
        this.shopFinished = shopFinished;
    }
}
