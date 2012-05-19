package de.minestar.buycraft.shops;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.minecraft.server.Packet130UpdateSign;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.utils.UtilPermissions;

import de.minestar.buycraft.core.Core;
import de.minestar.buycraft.core.Messages;
import de.minestar.buycraft.core.Permission;
import de.minestar.buycraft.manager.DatabaseManager;
import de.minestar.buycraft.manager.ItemManager;
import de.minestar.buycraft.units.BlockVector;
import de.minestar.buycraft.units.BuyCraftInventory;
import de.minestar.buycraft.units.BuyCraftStack;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class UserShop {

    private static int MAX_ITEMS = 64 * 27;

    private final int ShopID;
    private final BlockVector position;
    private BuyCraftInventory inventory;
    private final long creationTime;
    private long lastUsedTime;
    private boolean active = false;
    private boolean shopFinished = true;

    public UserShop(int ShopID, int x, int y, int z, String worldName, boolean active, boolean shopFinished, int creationTime, int lastUsedTime) {
        this.ShopID = ShopID;
        this.position = new BlockVector(x, y, z, worldName);
        this.inventory = new BuyCraftInventory();
        this.active = active;
        this.shopFinished = shopFinished;
        this.creationTime = creationTime;
        this.lastUsedTime = lastUsedTime;
    }

    public UserShop(int ShopID, BlockVector position, boolean active, boolean shopFinished, int creationTime, int lastUsedTime) {
        this.ShopID = ShopID;
        this.position = position;
        this.inventory = new BuyCraftInventory();
        this.active = active;
        this.shopFinished = shopFinished;
        this.creationTime = creationTime;
        this.lastUsedTime = lastUsedTime;
    }

    public void verifyCreationStatus(String[] lines) {
        this.shopFinished = lines[1].length() > 0 && !lines[1].equalsIgnoreCase(" ");
    }

    public UserShop(ResultSet result) throws SQLException {
        this(result.getInt(1), result.getInt(6), result.getInt(7), result.getInt(8), result.getString(9), result.getBoolean(2), result.getBoolean(3), result.getInt(4), result.getInt(5));
    }

    public BuyCraftInventory getInventory() {
        return this.inventory;
    }

    public BuyCraftInventory setInventory(BuyCraftInventory inventory) {
        this.inventory = inventory;
        return this.getInventory();
    }

    public BuyCraftStack getItem(int index) {
        return this.inventory.getItem(index);
    }

    public int getInventorySize() {
        return this.inventory.getSize();
    }

    public ArrayList<BuyCraftStack> getItems() {
        return this.inventory.getItems();
    }

    /**
     * @return the shopID
     */
    public int getShopID() {
        return ShopID;
    }

    /**
     * @return the creationTime
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * @return the lastUsedTime
     */
    public long getLastUsedTime() {
        return lastUsedTime;
    }

    /**
     * @return the position
     */
    public BlockVector getPosition() {
        return position;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the shopFinished
     */
    public boolean isShopFinished() {
        return shopFinished;
    }

    /**
     * @param lastUsedTime
     *            the lastUsedTime to set
     */
    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    /**
     * @param shopFinished
     *            the shopFinished to set
     */
    public void setShopFinished(boolean shopFinished) {
        this.shopFinished = shopFinished;
    }

    /**
     * Handle sign-interaction
     * 
     * @param shop
     * @param player
     */
    public void handleSignInteract(ShopType shop, Player player) {
        System.out.println("sign interact ");
    }

    private void handleBuy(ShopType shop, Player player, int[] buyRatio, int goldAmount, int itemID, short itemData) {
    }

    private void handleSell(ShopType shop, Player player, int[] sellRatio, int materialAmount, int itemID, short itemData) {
    }

    /**
     * Handle chest-interaction
     * 
     * @param shop
     * @param player
     */
    public void handleChestInteract(ShopType shop, Player player) {
        // SHOP FINISHED?
        if (!this.isShopFinished()) {
            PlayerUtils.sendError(player, Core.NAME, Messages.SHOP_NOT_FINISHED);
            return;
        }

        // SHOP ACTIVATED?
        if (!this.isActive()) {
            PlayerUtils.sendError(player, Core.NAME, Messages.SHOP_NOT_ACTIVATED);
            return;
        }

        // get ItemID & ItemData
        String[] split = ItemManager.extractItemLine(shop.getSign().getLine(1)).split(":");
        int itemID = ItemManager.getInstance().getItemId(split[0]);
        short itemData = 0;
        if (split.length > 1) {
            try {
                itemData = Short.valueOf(split[1]);
            } catch (Exception e) {
                PlayerUtils.sendError(player, Core.NAME, "Internal error [CHEST:001]. Please contact an admin!");
                return;
            }
        }

        // validate ItemID
        if (!ItemManager.getInstance().isItemIDAllowed(itemID)) {
            PlayerUtils.sendError(player, Core.NAME, Messages.ITEM_NOT_ALLOWED);
            return;
        }

        // print info
        int[] buyRatio = ItemManager.getRatio(shop.getSign().getLine(2));
        int[] sellRatio = ItemManager.getRatio(shop.getSign().getLine(3));

        // catch line 4 = empty (means: line 4 = line 3)
        if (shop.getSign().getLine(3).length() < 1) {
            sellRatio[0] = buyRatio[0];
            sellRatio[1] = buyRatio[1];
        }

        // create the ItemName [Name:SubID]
        String itemName = ItemManager.getNameForID(itemID);
        if (itemData != 0) {
            itemName += ":" + itemData;
        }

        // print infos
        if (buyRatio[0] > 0 && buyRatio[1] > 0) {
            PlayerUtils.sendMessage(player, ChatColor.GOLD, Core.NAME, "Verkauf: " + buyRatio[0] + "*'" + itemName + "' für " + buyRatio[1] + "*'" + Material.GOLD_INGOT.name() + "'.");
        }
        if (sellRatio[0] > 0 && sellRatio[1] > 0) {
            PlayerUtils.sendMessage(player, ChatColor.GOLD, Core.NAME, "Ankauf: " + sellRatio[0] + "*'" + itemName + "' für " + sellRatio[1] + "*'" + Material.GOLD_INGOT.name() + "'.");
        }
    }

    public void activateItem(PlayerInteractEvent event, ShopType shopType) {
        // only unfinished shops
        if (this.shopFinished)
            return;

        Player player = event.getPlayer();

        // check permissions
        if (!UtilPermissions.playerCanUseCommand(player, Permission.USER_SHOP_CREATE)) {
            PlayerUtils.sendError(player, Core.NAME, Messages.USER_SHOP_CREATE_ERROR);
            return;
        }

        // check item in hand
        if (player.getItemInHand() == null || player.getItemInHand().getTypeId() == Material.AIR.getId()) {
            PlayerUtils.sendError(player, Core.NAME, Messages.NO_ITEM_IN_HAND);
            return;
        }

        // check ItemID allowed
        int ID = player.getItemInHand().getTypeId();
        short data = player.getItemInHand().getDurability();
        if (!ItemManager.getInstance().isItemIDAllowed(ID)) {
            PlayerUtils.sendError(player, Core.NAME, Messages.ITEM_NOT_ALLOWED);
            return;
        }

        // update the sign
        String itemName = ItemManager.getInstance().getItemName(ID);
        if (data > 0) {
            itemName += ":" + data;
        }
        itemName = "{" + itemName + "}";
        if (itemName.length() > 15) {
            itemName = "" + ID;
            if (data > 0) {
                itemName += ":" + data;
            }
            itemName = "{" + itemName + "}";
        }
        shopType.getSign().setLine(1, itemName);
        shopType.getSign().update();

        // SEND UPDATE FOR CLIENTS => NEED HELP OF ORIGINAL MC-SERVERSOFTWARE
        CraftPlayer cPlayer = (CraftPlayer) player;
        Packet130UpdateSign signPacket = null;
        signPacket = new Packet130UpdateSign(shopType.getSign().getX(), shopType.getSign().getY(), shopType.getSign().getZ(), shopType.getSign().getLines());
        cPlayer.getHandle().netServerHandler.sendPacket(signPacket);

        this.shopFinished = DatabaseManager.getInstance().setUsershopFinished(this, true);;
        if (!this.shopFinished) {
            shopType.getSign().setLine(1, "");
            shopType.getSign().update();

            // SEND UPDATE FOR CLIENTS => NEED HELP OF ORIGINAL
            // MC-SERVERSOFTWARE
            signPacket = new Packet130UpdateSign(shopType.getSign().getX(), shopType.getSign().getY(), shopType.getSign().getZ(), shopType.getSign().getLines());
            cPlayer.getHandle().netServerHandler.sendPacket(signPacket);
            PlayerUtils.sendError(cPlayer, Core.NAME, Messages.USER_SHOP_INTERNAL_ERROR_0X03);
            PlayerUtils.sendInfo(cPlayer, Core.NAME, Messages.TRY_AGAIN_OR_CONTACT_ADMIN);
        } else {
            PlayerUtils.sendSuccess(cPlayer, Core.NAME, Messages.USER_SHOP_CREATE_SUCCESS);
        }
    }

    public boolean setActive(boolean active, Chest chest) {
        if (active) {
            this.saveInventoryToDB(chest);
            chest.getBlockInventory().clear();
        } else {
            this.fillChest(chest);
            this.clearInventoryInDB();
        }
        this.active = active;
        return this.active;
    }

    private void fillChest(Chest chest) {
        chest.getBlockInventory().clear();
        for (BuyCraftStack stack : this.inventory.getItems()) {
            ItemStack itemStack = new ItemStack(stack.getTypeID());
            itemStack.setAmount(stack.getAmount());
            itemStack.setDurability(stack.getSubID());
            chest.getBlockInventory().addItem(itemStack);
        }
    }

    private void clearInventoryInDB() {
        DatabaseManager.getInstance().removeInventory(this);
        this.inventory.clearItems();
    }

    private void saveInventoryToDB(Chest chest) {
        this.inventory.clearItems();
        ItemStack current;
        BuyCraftStack stack;
        for (int i = 0; i < chest.getInventory().getSize(); i++) {
            current = chest.getInventory().getItem(i);
            if (current == null || current.getTypeId() == Material.AIR.getId())
                continue;

            stack = DatabaseManager.getInstance().createItemStack(this, current.getTypeId(), current.getDurability(), current.getAmount());
            if (stack != null) {
                this.inventory.addItem(stack);
            }
        }
    }

    @Override
    public String toString() {
        return "UserShop={ POS=" + this.position.toString() + " }";
    }
}
