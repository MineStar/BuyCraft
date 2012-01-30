package com.bukkit.gemo.BuyCraft;

import java.io.Serializable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.BuyCraft.statics.Potion;
import com.bukkit.gemo.utils.UtilPermissions;

public class BCInfiniteShop extends BCShop implements Serializable {
    private static final long serialVersionUID = 3456581809245152700L;

    // /////////////////////////////////
    //
    // CONSTRUCTORS
    //
    // /////////////////////////////////
    public BCInfiniteShop() {
        super();
    }

    public BCInfiniteShop(String worldName, int x, int y, int z) {
        super(worldName, x, y, z);
    }

    // /////////////////////////////////
    //
    // HANDLE LEFTCLICK
    //
    // /////////////////////////////////
    public void handleLeftClick(Player player, Sign sign, Chest chest) {
        String[] itemSplit = BCShop.getSplit(BCShop.getSpecialTextOnLine(sign.getLine(1), "{", "}"));
        Integer[] buyRatios = BCShop.getRatios(sign.getLine(2));
        Integer[] sellRatios = buyRatios;
        if (sign.getLine(3).length() > 0)
            sellRatios = BCShop.getRatios(sign.getLine(3));

        int sellItemId = 0;
        short sellItemData = 0;
        try {
            sellItemId = BCCore.getItemId(itemSplit[0]);
            sellItemData = Short.valueOf(itemSplit[1]);
        } catch (Exception e) {
            System.out.println("LOCATION: " + sign.getBlock().getLocation().toString());
            e.printStackTrace();
            return;
        }

        if (!BCCore.isAllowedItem(itemSplit[0]))
            return;

        String itemName = Material.getMaterial(sellItemId).name();
        if (sellItemId == Material.POTION.getId())
            itemName = Potion.getName(sellItemData);

        if (buyRatios[0] > 0 && buyRatios[1] > 0)
            BCChatUtils.printInfo(player, ChatColor.GOLD, "KAUFEN: " + buyRatios[0] + " '" + itemName + "' für " + buyRatios[1] + " Goldbarren.");
        else
            BCChatUtils.printInfo(player, ChatColor.GOLD, "Dieser Shop verkauft nichts.");
        if (sellRatios[0] > 0 && sellRatios[1] > 0)
            BCChatUtils.printInfo(player, ChatColor.GOLD, "VERKAUFEN: " + sellRatios[0] + " '" + itemName + "' für " + sellRatios[1] + " Goldbarren.");
        else
            BCChatUtils.printInfo(player, ChatColor.GOLD, "Dieser Shop kauft nichts an.");
    }

    // /////////////////////////////////
    //
    // HANDLE RIGHTCLICK
    //
    // /////////////////////////////////
    public void handleRightClick(Player player, Sign sign, Chest chest) {
        String[] itemSplit = BCShop.getSplit(BCShop.getSpecialTextOnLine(sign.getLine(1), "{", "}"));
        Integer[] buyRatios = BCShop.getRatios(sign.getLine(2));
        Integer[] sellRatios = buyRatios;
        if (sign.getLine(3).length() > 0)
            sellRatios = BCShop.getRatios(sign.getLine(3));

        int sellItemId = 0;
        short sellItemData = 0;

        try {
            sellItemId = BCCore.getItemId(itemSplit[0]);
            sellItemData = Short.valueOf(itemSplit[1]);
        } catch (Exception e) {
            System.out.println("LOCATION: " + sign.getBlock().getLocation().toString());
            e.printStackTrace();
            return;
        }

        if (!BCCore.isAllowedItem(itemSplit[0]))
            return;

        int sellItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), sellItemId, sellItemData);
        int nuggetItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), Material.GOLD_NUGGET.getId());
        int goldIngotItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), Material.GOLD_INGOT.getId());
        int goldBlockItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), Material.GOLD_BLOCK.getId());
        nuggetItemCountInChest = nuggetItemCountInChest + (9 * goldIngotItemCountInChest) + (9 * 9 * goldBlockItemCountInChest);

        // ////////////////////////////
        // CATCH OTHER/WRONG ITEMS
        // ////////////////////////////
        for (ItemStack item : chest.getInventory().getContents()) {
            if (item == null)
                continue;

            if (item.getTypeId() < 1)
                continue;

            if ((item.getTypeId() != sellItemId && item.getTypeId() != Material.GOLD_INGOT.getId() && item.getTypeId() != Material.GOLD_NUGGET.getId() && item.getTypeId() != Material.GOLD_BLOCK.getId()) || (item.getTypeId() == sellItemId && item.getDurability() != sellItemData)) {
                if (buyRatios[0] > 0 && buyRatios[1] > 0)
                    BCChatUtils.printInfo(player, ChatColor.GOLD, "KAUFEN: " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "' für " + buyRatios[1] + " Goldbarren.");
                if (sellRatios[0] > 0 && sellRatios[1] > 0)
                    BCChatUtils.printInfo(player, ChatColor.GOLD, "VERKAUFEN: " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' für " + sellRatios[1] + " Goldbarren.");

                return;
            }
        }

        // ////////////////////////////
        // CATCH SELL & BUY
        // ////////////////////////////
        String itemName = Material.getMaterial(sellItemId).name();
        if (sellItemId == Material.POTION.getId())
            itemName = Potion.getName(sellItemData);

        if (sellItemCountInChest == 0 && nuggetItemCountInChest == 0) {
            if (buyRatios[0] > 0 && buyRatios[1] > 0)
                BCChatUtils.printInfo(player, ChatColor.GOLD, "KAUFEN " + buyRatios[0] + " '" + itemName + "' für " + buyRatios[1] + " Goldbarren.");
            if (sellRatios[0] > 0 && sellRatios[1] > 0)
                BCChatUtils.printInfo(player, ChatColor.GOLD, "VERKAUFEN: " + sellRatios[0] + " '" + itemName + "' für " + sellRatios[1] + " Goldbarren.");
            return;
        }
        if (sellItemCountInChest > 0 && nuggetItemCountInChest > 0) {
            BCChatUtils.printError(player, "Du kannst nur kaufen ODER verkaufen, nicht beides zugleich.");
            return;
        }

        // ////////////////////////////
        // SELL / BUY ITEMS
        // ////////////////////////////
        if (nuggetItemCountInChest > sellItemCountInChest) {
            if (buyRatios[0] > 0 && buyRatios[1] > 0) {
                /** CHECK PERMISSION */
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.buy." + Material.getMaterial(sellItemId).name().toLowerCase())) {
                    BCChatUtils.printError(player, "Du darfst kein '" + itemName + "' kaufen.");
                    return;
                }

                /** BUY ITEMS */
                float blockPerNugget = (float) ((float) buyRatios[0] / (float) buyRatios[1] / 9.0f);
                double bBlocks = Math.floor(blockPerNugget * nuggetItemCountInChest);
                int boughtBlocks = (int) bBlocks;
                double ratio = ((double) buyRatios[0] / (double) buyRatios[1]);
                int restGoldNuggets = (int) (nuggetItemCountInChest - (boughtBlocks / ratio * 9));

                // AT LEAST ONE BLOCK MUST BE BOUGHT
                if (boughtBlocks < 1) {
                    BCChatUtils.printError(player, "Du bekommst keine Items für " + nuggetItemCountInChest + " Goldnuggets.");
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte mehr Gold in die Kiste legen.");
                    return;
                }

                // MORE BLOCKS THAN INVENTORYSIZE?
                if (boughtBlocks > 27 * 64) {
                    BCChatUtils.printError(player, "Du kannst nur maximal " + (27 * 64) + " Items auf einmal kaufen.");
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "Du hast versucht " + boughtBlocks + " Items zu kaufen.");
                    return;
                }

                // CLEAR INVENTORY
                chest.getInventory().clear();

                // ADD ITEM
                ItemStack newItem = new ItemStack(sellItemId, boughtBlocks);
                if (sellItemData > 0)
                    newItem.setDurability(sellItemData);
                chest.getInventory().addItem(newItem);

                // ADD RESTGOLD
                if (restGoldNuggets > 0) {
                    int restIngotCount = (int) Math.floor(restGoldNuggets / 9);
                    restGoldNuggets = restGoldNuggets - restIngotCount * 9;
                    int restBlockCount = (int) Math.floor(restIngotCount / 9);
                    restIngotCount = restIngotCount - restBlockCount * 9;

                    if (restGoldNuggets > 0) {
                        ItemStack restNuggets = new ItemStack(Material.GOLD_NUGGET.getId(), restGoldNuggets);
                        chest.getInventory().addItem(restNuggets);
                    }
                    if (restIngotCount > 0) {
                        ItemStack restIngots = new ItemStack(Material.GOLD_INGOT.getId(), restIngotCount);
                        chest.getInventory().addItem(restIngots);
                    }
                    if (restBlockCount > 0) {
                        ItemStack restGBlocks = new ItemStack(Material.GOLD_BLOCK.getId(), restBlockCount);
                        chest.getInventory().addItem(restGBlocks);
                    }
                }

                // PRINT SUCCESS
                BCChatUtils.printInfo(player, ChatColor.GOLD, "Du hast " + boughtBlocks + " x '" + itemName + "' für " + nuggetItemCountInChest + " Goldnuggets gekauft.");
                return;
            } else {
                BCChatUtils.printInfo(player, ChatColor.RED, "Dieser Shop verkauft nichts.");
                return;
            }
        } else {
            if (sellRatios[0] > 0 && sellRatios[1] > 0) {
                /** CHECK PERMISSION */
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.sell." + Material.getMaterial(sellItemId).name().toLowerCase())) {
                    BCChatUtils.printError(player, "Du darfst kein '" + itemName + "' verkaufen.");
                    return;
                }

                /** SELL ITEMS */
                float blocksPerNugget = (float) ((float) sellRatios[0] / (float) sellRatios[1] / 9.0f);
                float bNuggets = (float) ((float) sellItemCountInChest / (float) blocksPerNugget);
                int boughtNuggets = (int) (Math.floor(bNuggets));
                int goldIngotCount = (int) Math.floor(boughtNuggets / 9);
                int goldNuggetCount = boughtNuggets - goldIngotCount * 9;
                int goldBlockCount = (int) Math.floor(goldIngotCount / 9);
                goldIngotCount = goldIngotCount - goldBlockCount * 9;

                // GET STACKCOUNTS
                int stacksizeGoldNugget = (int) Math.ceil((float) goldNuggetCount / 64);
                int stacksizeGoldIngot = (int) Math.ceil((float) goldIngotCount / 64);
                int stacksizeGoldBlock = (int) Math.ceil((float) goldBlockCount / 64);

                // AT LEAST ONE NUGGET/INGOT/BLOCK IS NEEDED
                if (goldNuggetCount < 1 && goldIngotCount < 1 && goldBlockCount < 1) {
                    BCChatUtils.printError(player, "Für " + sellItemCountInChest + " x '" + itemName + "' bekommst du keine Goldnuggets.");
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte mehr Items in die Kiste legen.");
                    return;
                }

                // MORE BLOCKS THAN INVENTORYSIZE?
                if (stacksizeGoldNugget + stacksizeGoldIngot + stacksizeGoldBlock > 27) {
                    BCChatUtils.printError(player, "Du hast versucht zu viel auf einmal zu verkaufen.");
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte entferne ein paar Items.");
                    return;
                }

                // CLEAR INVENTORY
                chest.getInventory().clear();

                // ADD GOLD
                if (goldNuggetCount > 0) {
                    ItemStack newGoldNugget = new ItemStack(Material.GOLD_NUGGET.getId(), goldNuggetCount);
                    chest.getInventory().addItem(newGoldNugget);
                }
                if (goldIngotCount > 0) {
                    ItemStack newGoldIngot = new ItemStack(Material.GOLD_INGOT.getId(), goldIngotCount);
                    chest.getInventory().addItem(newGoldIngot);
                }
                if (goldBlockCount > 0) {
                    ItemStack newGoldBlock = new ItemStack(Material.GOLD_BLOCK.getId(), goldBlockCount);
                    chest.getInventory().addItem(newGoldBlock);
                }

                // PRINT INFO
                String text = "";
                if (goldNuggetCount > 0) {
                    text += goldNuggetCount + " Goldnuggets";
                }
                if (goldIngotCount > 0) {
                    if (!text.equalsIgnoreCase(""))
                        text += ", ";
                    text += goldIngotCount + " Goldbarren";
                }
                if (goldBlockCount > 0) {
                    if (!text.equalsIgnoreCase(""))
                        text += ", ";
                    text += goldBlockCount + " Goldblöcke";
                }
                BCChatUtils.printInfo(player, ChatColor.GOLD, "Du hast " + sellItemCountInChest + " x '" + itemName + "' für " + text + " verkauft.");
                return;
            } else {
                BCChatUtils.printInfo(player, ChatColor.RED, "Dieser Shop kauft nichts an.");
                return;
            }
        }
    }
}
