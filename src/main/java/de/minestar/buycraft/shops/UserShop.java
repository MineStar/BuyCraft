package de.minestar.buycraft.shops;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
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
import de.minestar.buycraft.units.EnumPotion;
import de.minestar.buycraft.units.PersistentBuyCraftStack;
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

    private UserShop(int ShopID, int x, int y, int z, String worldName, boolean active, boolean shopFinished, int creationTime, int lastUsedTime) {
        this.ShopID = ShopID;
        this.position = new BlockVector(worldName, x, y, z);
        this.inventory = new BuyCraftInventory();
        this.active = active;
        this.shopFinished = shopFinished;
        this.creationTime = creationTime;
        this.lastUsedTime = lastUsedTime;
    }

    public boolean isValid() {
        World world = Bukkit.getWorld(this.position.getWorldName());
        if (world == null)
            return false;
        Location location = new Location(world, this.position.getX(), this.position.getY(), this.position.getZ());
        return location.getBlock().getTypeId() == Material.WALL_SIGN.getId() && location.getBlock().getRelative(BlockFace.DOWN).getTypeId() == Material.CHEST.getId();
    }

    public void verifyCreationStatus(String[] lines) {
        this.shopFinished = lines[1].length() > 0 && !lines[1].equalsIgnoreCase(" ");
    }

    public UserShop(ResultSet result) throws SQLException {
        this(result.getInt("ID"), result.getInt("xPos"), result.getInt("yPos"), result.getInt("zPos"), result.getString("worldName"), result.getBoolean("isActive"), result.getBoolean("shopFinished"), result.getInt("creationTime"), result.getInt("lastUsedTime"));
    }

    public BuyCraftInventory getInventory() {
        return this.inventory;
    }

    public BuyCraftInventory setInventory(BuyCraftInventory inventory) {
        this.inventory = inventory;
        return this.getInventory();
    }

    public BuyCraftInventory addItem(PersistentBuyCraftStack stack) {
        this.inventory.addItem(stack);
        return this.inventory;
    }

    public PersistentBuyCraftStack getItem(int index) {
        return this.inventory.getItem(index);
    }

    public int getInventorySize() {
        return this.inventory.getSize();
    }

    public ArrayList<PersistentBuyCraftStack> getItems() {
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
    public void handleSignInteractByOtherPlayer(ShopType shop, Player player) {
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
                PlayerUtils.sendError(player, Core.NAME, "Internal error [SIGN:001]. Please contact an admin!");
                return;
            }
        }

        // validate ItemID
        if (!ItemManager.getInstance().isItemIDAllowed(itemID)) {
            PlayerUtils.sendError(player, Core.NAME, "Diese ID ist nicht freigeschaltet.");
            return;
        }

        // test if only gold or the item is in the chest
        int itemAmount = ItemManager.countItemInInventory(shop, Material.GOLD_INGOT.getId());
        itemAmount += ItemManager.countItemInInventory(shop, Material.GOLD_BLOCK.getId());
        itemAmount += ItemManager.countItemInInventory(shop, itemID, itemData);
        if (itemAmount != ItemManager.countAllItemsInInventory(shop)) {
            PlayerUtils.sendError(player, Core.NAME, "Es kann nur Gold und das Item in die Kiste gelegt werden.");
            return;
        }

        // count materials in chest
        int goldAmount = ItemManager.countItemInInventory(shop, Material.GOLD_INGOT.getId());
        goldAmount += (ItemManager.countItemInInventory(shop, Material.GOLD_BLOCK.getId()) * 9);
        int materialAmount = ItemManager.countItemInInventory(shop, itemID, itemData);

        // only sell OR buy, not both
        if (goldAmount > 0 && materialAmount > 0) {
            PlayerUtils.sendError(player, Core.NAME, "Du kannst nur kaufen ODER verkaufen. Nicht beides auf einmal.");
            return;
        }

        if (goldAmount > 0) {
            // BUY BLOCKS
            int[] buyRatio = ItemManager.getRatio(shop.getSign().getLine(2));
            if (buyRatio[0] > 0 && buyRatio[1] > 0) {
                // check permissions
                String itemName = ItemManager.getNameForID(itemID);
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.buy." + itemName.toLowerCase())) {
                    PlayerUtils.sendError(player, Core.NAME, "Du darfst kein '" + itemName + "' kaufen.");
                    return;
                }
                this.handleBuy(shop, player, buyRatio, goldAmount, itemID, itemData);
                return;
            } else {
                PlayerUtils.sendError(player, Core.NAME, "Dieser Shop verkauft derzeit nicht.");
                return;
            }
        } else if (materialAmount > 0) {
            // SELL BLOCKS
            int[] sellRatio = ItemManager.getRatio(shop.getSign().getLine(3));
            if (shop.getSign().getLine(3).length() < 1) {
                sellRatio = ItemManager.getRatio(shop.getSign().getLine(2));
            }
            if (sellRatio[0] > 0 && sellRatio[1] > 0) {
                // check permissions
                String itemName = ItemManager.getNameForID(itemID);
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.sell." + itemName.toLowerCase())) {
                    PlayerUtils.sendError(player, Core.NAME, "Du darfst kein '" + itemName + "' verkaufen.");
                    return;
                }
                this.handleSell(shop, player, sellRatio, materialAmount, itemID, itemData);
                return;
            } else {
                PlayerUtils.sendError(player, Core.NAME, "Dieser Shop kauft derzeit nicht an.");
                return;
            }
        } else {
            // PRINT INFO
            this.handleChestInteract(shop, player);
            return;
        }
    }

    private void handleBuy(ShopType shop, Player player, int[] buyRatio, int goldAmount, int itemID, short itemData) {
        float ratio = (float) buyRatio[0] / (float) buyRatio[1];
        int restAmount = goldAmount % buyRatio[1];
        goldAmount -= restAmount;
        int wantAmount = (int) (ratio * goldAmount);

        // CHECK IF THERE IS ENOUGH SPACE
        if (wantAmount + restAmount > MAX_ITEMS) {
            PlayerUtils.sendError(player, Core.NAME, "Du kannst nicht mehr als '" + MAX_ITEMS + "' Items auf einmal kaufen.");
            return;
        }

        // THERE MUST BE AT LEAST 1 ITEM
        if (wantAmount < 1) {
            PlayerUtils.sendError(player, Core.NAME, "Du musst mindestens 1 Item kaufen.");
            return;
        }

        // HAS THE SHOP ENOUGH MATERIAL?
        int hasAmount = this.inventory.countItemStack(itemID, itemData);
        if (wantAmount > hasAmount) {
            PlayerUtils.sendError(player, Core.NAME, "Der Shop hat nicht so viel auf Lager!");
            PlayerUtils.sendInfo(player, Core.NAME, "Du wolltest kaufen: " + wantAmount + ".");
            PlayerUtils.sendInfo(player, Core.NAME, "Der Shop hat: " + hasAmount + ".");
            return;
        }

        // clear old inventory
        shop.getChest().getInventory().clear();

        // add item
        ItemStack item = new ItemStack(itemID);
        if (itemData != 0) {
            item.setDurability(itemData);
        }
        item.setAmount(wantAmount);
        shop.getChest().getInventory().addItem(item);

        // add rest-gold
        if (restAmount > 0) {
            item = new ItemStack(Material.GOLD_INGOT.getId());
            item.setAmount(restAmount);
            shop.getChest().getInventory().addItem(item);
        }

        // create the ItemName [Name:SubID]
        String itemName = ItemManager.getNameForID(itemID);
        if (itemData != 0) {
            itemName += ":" + itemData;
        }

        // workaround for potions
        if (itemID == Material.POTION.getId()) {
            String tmpName = EnumPotion.getName(itemData);
            if (tmpName != null) {
                itemName = tmpName;
            }
        }

        // update the amounts of the store internally (CRITICAL!!!)
        boolean result = this.inventory.removeItem(this, itemID, itemData, wantAmount) && this.inventory.addItem(this, Material.GOLD_INGOT.getId(), (short) 0, goldAmount);
        if (!result) {
            shop.getChest().getInventory().clear();
            PlayerUtils.sendError(player, Core.NAME, "Beim Kauf ist ein Fehler aufgetreten! Bitte wende dich an einen Admin! [BUY:0x100]");
            return;
        }

        // print info
        PlayerUtils.sendMessage(player, ChatColor.DARK_GREEN, Core.NAME, "Du hast " + wantAmount + "*'" + itemName + "' für " + goldAmount + "*'" + Material.GOLD_INGOT.name() + "' gekauft.");
        if (restAmount > 0) {
            PlayerUtils.sendMessage(player, ChatColor.GRAY, Core.NAME, "Rest: " + restAmount + "*'" + Material.GOLD_INGOT.name() + "'");
        }
    }

    private void handleSell(ShopType shop, Player player, int[] sellRatio, int materialAmount, int itemID, short itemData) {
        float ratio = (float) sellRatio[1] / (float) sellRatio[0];
        int restAmount = materialAmount % sellRatio[0];
        materialAmount -= restAmount;
        int wantAmount = (int) (ratio * materialAmount);

        // CHECK IF THERE IS ENOUGH SPACE
        if (wantAmount + restAmount > MAX_ITEMS) {
            PlayerUtils.sendError(player, Core.NAME, "Du kannst nicht mehr als '" + MAX_ITEMS + "' Items auf einmal verkaufen.");
            return;
        }

        // THERE MUST BE AT LEAST 1 ITEM
        if (wantAmount < 1) {
            PlayerUtils.sendError(player, Core.NAME, "Du musst mindestens 1 Item verkaufen.");
            return;
        }

        // HAS THE SHOP ENOUGH MATERIAL?
        int hasAmount = this.inventory.countItemStack(Material.GOLD_INGOT.getId(), (short) 0);
        if (wantAmount > hasAmount) {
            PlayerUtils.sendError(player, Core.NAME, "Der Shop hat nicht so viel Gold auf Lager!");
            PlayerUtils.sendInfo(player, Core.NAME, "Du wolltest haben: " + wantAmount + ".");
            PlayerUtils.sendInfo(player, Core.NAME, "Der Shop hat: " + hasAmount + ".");
            return;
        }

        // clear old inventory
        shop.getChest().getInventory().clear();

        // add gold
        ItemStack item = new ItemStack(Material.GOLD_INGOT.getId());
        item.setAmount(wantAmount);
        shop.getChest().getInventory().addItem(item);

        // add rest-item
        if (restAmount > 0) {
            item = new ItemStack(itemID);
            if (itemData != 0) {
                item.setDurability(itemData);
            }
            item.setAmount(restAmount);
            shop.getChest().getInventory().addItem(item);
        }

        // create the ItemName [Name:SubID]
        String itemName = ItemManager.getNameForID(itemID);
        if (itemData != 0) {
            itemName += ":" + itemData;
        }

        // workaround for potions
        if (itemID == Material.POTION.getId()) {
            String tmpName = EnumPotion.getName(itemData);
            if (tmpName != null) {
                itemName = tmpName;
            }
        }

        // update the amounts of the store internally (CRITICAL!!!)
        boolean result = this.inventory.removeItem(this, Material.GOLD_INGOT.getId(), (short) 0, wantAmount) && this.inventory.addItem(this, itemID, itemData, materialAmount);
        if (!result) {
            shop.getChest().getInventory().clear();
            PlayerUtils.sendError(player, Core.NAME, "Beim Kauf ist ein Fehler aufgetreten! Bitte wende dich an einen Admin! [BUY:0x100]");
            return;
        }

        // print info
        PlayerUtils.sendMessage(player, ChatColor.DARK_GREEN, Core.NAME, "Du hast " + materialAmount + "*'" + itemName + "' für " + wantAmount + "*'" + Material.GOLD_INGOT.name() + "' verkauft.");
        if (restAmount > 0) {
            PlayerUtils.sendMessage(player, ChatColor.GRAY, Core.NAME, "Rest: " + restAmount + "*'" + itemName + "'");
        }
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

        // workaround for potions
        if (itemID == Material.POTION.getId()) {
            String tmpName = EnumPotion.getName(itemData);
            if (tmpName != null) {
                itemName = tmpName;
            }
        }

        // print infos
        if (buyRatio[0] > 0 && buyRatio[1] > 0) {
            PlayerUtils.sendMessage(player, ChatColor.BLUE, Core.NAME, "Verkauf: " + buyRatio[0] + "*'" + itemName + "' für " + buyRatio[1] + "*'" + Material.GOLD_INGOT.name() + "'. (Lager: " + this.inventory.countItemStack(itemID, itemData) + ")");
        }
        if (sellRatio[0] > 0 && sellRatio[1] > 0) {
            PlayerUtils.sendMessage(player, ChatColor.BLUE, Core.NAME, "Ankauf: " + sellRatio[0] + "*'" + itemName + "' für " + sellRatio[1] + "*'" + Material.GOLD_INGOT.name() + "'.");
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
        // Packet130UpdateSign signPacket = null;
        // signPacket = new Packet130UpdateSign(shopType.getSign().getX(), shopType.getSign().getY(), shopType.getSign().getZ(), shopType.getSign().getLines());
        // cPlayer.getHandle().netServerHandler.sendPacket(signPacket);

        this.shopFinished = DatabaseManager.getInstance().setUsershopFinished(this, true);;
        if (!this.shopFinished) {
            shopType.getSign().setLine(1, "");
            shopType.getSign().update();

            // SEND UPDATE FOR CLIENTS => NEED HELP OF ORIGINAL
            // MC-SERVERSOFTWARE
            // signPacket = new Packet130UpdateSign(shopType.getSign().getX(), shopType.getSign().getY(), shopType.getSign().getZ(), shopType.getSign().getLines());
            // cPlayer.getHandle().netServerHandler.sendPacket(signPacket);
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
            this.dropChestInventory(chest);
            this.fillChest(chest);
            this.clearInventoryInDB();
        }
        this.active = active;
        return this.active;
    }

    private void dropChestInventory(Chest chest) {
        Location location = chest.getBlock().getRelative(BlockFace.UP).getLocation();
        ItemStack current;
        for (int i = 0; i < chest.getInventory().getSize(); i++) {
            current = chest.getInventory().getItem(i);
            if (current == null || current.getTypeId() == Material.AIR.getId())
                continue;
            chest.getWorld().dropItem(location, current);
        }
    }

    private void fillChest(Chest chest) {
        chest.getBlockInventory().clear();
        for (PersistentBuyCraftStack stack : this.inventory.getItems()) {
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
        PersistentBuyCraftStack stack;
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
        return "UserShop={ " + this.position.toString() + " ; " + this.inventory.toString() + " }";
    }
}
