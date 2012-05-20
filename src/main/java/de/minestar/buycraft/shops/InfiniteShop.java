package de.minestar.buycraft.shops;

import net.minecraft.server.Packet130UpdateSign;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.utils.UtilPermissions;

import de.minestar.buycraft.core.Core;
import de.minestar.buycraft.core.Messages;
import de.minestar.buycraft.core.Permission;
import de.minestar.buycraft.manager.ItemManager;
import de.minestar.buycraft.units.EnumPotion;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class InfiniteShop {

    private static int MAX_ITEMS = 64 * 27;

    /**
     * Handle sign-interaction
     * 
     * @param shop
     * @param player
     */
    public static void handleSignInteract(ShopType shop, Player player) {
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
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.buy." + itemName.toLowerCase())) {
                    PlayerUtils.sendError(player, Core.NAME, "Du darfst kein '" + itemName + "' kaufen.");
                    return;
                }
                handleBuy(shop, player, buyRatio, goldAmount, itemID, itemData);
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
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.sell." + itemName.toLowerCase())) {
                    PlayerUtils.sendError(player, Core.NAME, "Du darfst kein '" + itemName + "' verkaufen.");
                    return;
                }
                handleSell(shop, player, sellRatio, materialAmount, itemID, itemData);
                return;
            } else {
                PlayerUtils.sendError(player, Core.NAME, "Dieser Shop kauft derzeit nicht an.");
                return;
            }
        } else {
            // PRINT INFO
            InfiniteShop.handleChestInteract(shop, player);
            return;
        }
    }

    private static void handleBuy(ShopType shop, Player player, int[] buyRatio, int goldAmount, int itemID, short itemData) {
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

        // print info
        PlayerUtils.sendMessage(player, ChatColor.GOLD, Core.NAME, "Du hast " + wantAmount + "*'" + itemName + "' für " + goldAmount + "*'" + Material.GOLD_INGOT.name() + "' gekauft.");
        if (restAmount > 0) {
            PlayerUtils.sendMessage(player, ChatColor.GRAY, Core.NAME, "Rest: " + restAmount + "*'" + Material.GOLD_INGOT.name() + "'");
        }
    }

    private static void handleSell(ShopType shop, Player player, int[] sellRatio, int materialAmount, int itemID, short itemData) {
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

        // print info
        PlayerUtils.sendMessage(player, ChatColor.GOLD, Core.NAME, "Du hast " + materialAmount + "*'" + itemName + "' für " + wantAmount + "*'" + Material.GOLD_INGOT.name() + "' verkauft.");
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
    public static void handleChestInteract(ShopType shop, Player player) {
        if (shop.getSign().getLine(1).length() < 1) {
            PlayerUtils.sendError(player, Core.NAME, Messages.SHOP_NOT_FINISHED);
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
            PlayerUtils.sendMessage(player, ChatColor.BLUE, Core.NAME, "Verkauf: " + buyRatio[0] + "*'" + itemName + "' für " + buyRatio[1] + "*'" + Material.GOLD_INGOT.name() + "'.");
        }
        if (sellRatio[0] > 0 && sellRatio[1] > 0) {
            PlayerUtils.sendMessage(player, ChatColor.BLUE, Core.NAME, "Ankauf: " + sellRatio[0] + "*'" + itemName + "' für " + sellRatio[1] + "*'" + Material.GOLD_INGOT.name() + "'.");
        }
    }

    public static void activateItem(PlayerInteractEvent event, ShopType shop) {
        Player player = event.getPlayer();

        // check permissions
        if (!UtilPermissions.playerCanUseCommand(player, Permission.INFINITE_SHOP_CREATE)) {
            PlayerUtils.sendError(player, Core.NAME, Messages.INFINITE_SHOP_CREATE_ERROR);
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
        shop.getSign().setLine(1, itemName);
        shop.getSign().update();

        // SEND UPDATE FOR CLIENTS => NEED HELP OF ORIGINAL MC-SERVERSOFTWARE
        CraftPlayer cPlayer = (CraftPlayer) player;
        Packet130UpdateSign signPacket = null;
        signPacket = new Packet130UpdateSign(shop.getSign().getX(), shop.getSign().getY(), shop.getSign().getZ(), shop.getSign().getLines());
        cPlayer.getHandle().netServerHandler.sendPacket(signPacket);
        PlayerUtils.sendSuccess(cPlayer, Core.NAME, Messages.INFINITE_SHOP_CREATE_SUCCESS_COMPLETE);
    }
}
