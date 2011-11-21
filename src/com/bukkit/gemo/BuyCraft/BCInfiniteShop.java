package com.bukkit.gemo.BuyCraft;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.utils.UtilPermissions;

public class BCInfiniteShop extends BCShop implements Serializable {
    private static final long serialVersionUID = 3456581809245152700L;
    private DecimalFormat decimalFormat = null;

    // /////////////////////////////////
    //
    // CONSTRUCTORS
    //
    // /////////////////////////////////
    public BCInfiniteShop() {
        super();
        decimalFormat = new DecimalFormat("#.##");
    }

    public BCInfiniteShop(String worldName, int x, int y, int z) {
        super(worldName, x, y, z);
    }

    // /////////////////////////////////
    //
    // GET
    //
    // /////////////////////////////////

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

        int sellItemId = BCCore.getItemId(itemSplit[0]);
        byte sellItemData = Byte.valueOf(itemSplit[1]);

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
                    BCChatUtils.printInfo(player, ChatColor.GOLD, "You can BUY " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + buyRatios[1] + " goldingots.");
                if (sellRatios[0] > 0 && sellRatios[1] > 0)
                    BCChatUtils.printInfo(player, ChatColor.GOLD, "You can SELL " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + sellRatios[1] + " goldingots.");

                return;
            }
        }

        // ////////////////////////////
        // CATCH SELL & BUY
        // ////////////////////////////
        if (sellItemCountInChest == 0 && nuggetItemCountInChest == 0) {
            if (buyRatios[0] > 0 && buyRatios[1] > 0)
                BCChatUtils.printInfo(player, ChatColor.GOLD, "You can BUY " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + buyRatios[1] + " goldingots.");
            if (sellRatios[0] > 0 && sellRatios[1] > 0)
                BCChatUtils.printInfo(player, ChatColor.GOLD, "You can SELL " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + sellRatios[1] + " goldingots.");
            return;
        }
        if (sellItemCountInChest > 0 && nuggetItemCountInChest > 0) {
            BCChatUtils.printError(player, "You can only sell OR buy things, not both at the same time.");
            return;
        }

        // ////////////////////////////
        // SELL / BUY ITEMS
        // ////////////////////////////
        if (nuggetItemCountInChest > sellItemCountInChest) {
            if (buyRatios[0] > 0 && buyRatios[1] > 0) {
                /** CHECK PERMISSION */
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.buy." + Material.getMaterial(sellItemId).name().toLowerCase())) {
                    BCChatUtils.printError(player, "You are not allowed to buy '" + Material.getMaterial(sellItemId).name() + "'.");
                    return;
                }

                /** BUY ITEMS */
                float blockPerNugget = (float) ((float) buyRatios[0] / (float) buyRatios[1] / 9.0f);
                double bBlocks = Math.floor(blockPerNugget * nuggetItemCountInChest);
                int boughtBlocks = (int) bBlocks;
                double restBlocks = (double) ((double) (blockPerNugget * nuggetItemCountInChest) - (double) boughtBlocks);
                
                // AT LEAST ONE BLOCK MUST BE BOUGHT
                if (boughtBlocks < 1) {
                    BCChatUtils.printError(player, "You cannot get any blocks for " + nuggetItemCountInChest + " goldnuggets.");
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "Please insert more nuggets.");
                    return;
                }

                // MORE BLOCKS THAN INVENTORYSIZE?
                if (boughtBlocks > 27 * 64) {
                    BCChatUtils.printError(player, "The maximum amount of items you can buy at once is " + (27 * 64));
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "You tried to buy " + boughtBlocks);
                    return;
                }

                // CLEAR INVENTORY
                chest.getInventory().clear();

                // ADD ITEM
                ItemStack newItem = new ItemStack(sellItemId, boughtBlocks);
                if (sellItemData > 0)
                    newItem.setDurability(sellItemData);
                chest.getInventory().addItem(newItem);

                // PRINT SUCCESS
                BCChatUtils.printInfo(player, ChatColor.GOLD, "You bought " + boughtBlocks + " '" + Material.getMaterial(sellItemId) + "' for " + nuggetItemCountInChest + " goldnuggets.");
                if (restBlocks > 0f) {
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "You lost " + decimalFormat.format(restBlocks) + " '" + Material.getMaterial(sellItemId) + "' in this transaction.");
                }
                return;
            } else {
                BCChatUtils.printInfo(player, ChatColor.RED, "This shop does not sell anything.");
                return;
            }
        } else {
            if (sellRatios[0] > 0 && sellRatios[1] > 0) {
                /** CHECK PERMISSION */
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.sell." + Material.getMaterial(sellItemId).name().toLowerCase())) {
                    BCChatUtils.printError(player, "You are not allowed to sell '" + Material.getMaterial(sellItemId).name() + "'.");
                    return;
                }

                /** SELL ITEMS */
                float blocksPerNugget = (float) ((float) sellRatios[0] / (float) sellRatios[1] / 9.0f);
                float bNuggets = (float) ((float)sellItemCountInChest / (float)blocksPerNugget);
                int boughtNuggets = (int) (Math.floor(bNuggets));
                int goldIngotCount = (int) Math.floor(boughtNuggets / 9);
                int goldNuggetCount = boughtNuggets - goldIngotCount*9;                
                int goldBlockCount = (int) Math.floor(goldIngotCount / 9);
                goldIngotCount = goldIngotCount - goldBlockCount*9;                  
                
                // GET STACKCOUNTS
                int stacksizeGoldNugget = (int) Math.ceil((float)goldNuggetCount / 64);
                int stacksizeGoldIngot = (int) Math.ceil((float)goldIngotCount / 64);
                int stacksizeGoldBlock = (int) Math.ceil((float)goldBlockCount / 64);

                // AT LEAST ONE NUGGET/INGOT/BLOCK IS NEEDED
                if (goldNuggetCount < 1 && goldIngotCount < 1 && goldBlockCount < 1) {
                    BCChatUtils.printError(player, "You cannot get any gold for " + sellItemCountInChest + " of '" + Material.getMaterial(sellItemId).name().toLowerCase() + "'.");
                    BCChatUtils.printInfo(player, ChatColor.GRAY, "Please insert more blocks.");
                    return;
                }

                // MORE BLOCKS THAN INVENTORYSIZE?
                if (stacksizeGoldNugget + stacksizeGoldIngot + stacksizeGoldBlock > 27) {
                    BCChatUtils.printError(player, "You want to sell to many blocks.");
                    return;
                }

                // CLEAR INVENTORY
                chest.getInventory().clear();
                
                // ADD GOLD
                if(goldNuggetCount > 0) {
                    ItemStack newGoldNugget = new ItemStack(Material.GOLD_NUGGET.getId(), goldNuggetCount);
                    chest.getInventory().addItem(newGoldNugget);
                }
                if(goldIngotCount > 0) {
                    ItemStack newGoldIngot = new ItemStack(Material.GOLD_INGOT.getId(), goldIngotCount);
                    chest.getInventory().addItem(newGoldIngot);
                }
                if(goldBlockCount > 0) {
                    ItemStack newGoldBlock = new ItemStack(Material.GOLD_BLOCK.getId(), goldBlockCount);
                    chest.getInventory().addItem(newGoldBlock);
                }

                // PRINT INFO
                String text = "";
                if(goldNuggetCount > 0) {
                    text += goldNuggetCount + " goldnuggets";
                }
                if(goldIngotCount > 0) {
                    if(!text.equalsIgnoreCase(""))
                        text += ", ";
                    text += goldIngotCount + " goldingots";
                }
                if(goldBlockCount > 0) {
                    if(!text.equalsIgnoreCase(""))
                        text += ", ";
                    text += goldBlockCount + " goldblocks";
                }
                BCChatUtils.printInfo(player, ChatColor.GOLD, "You sold " + sellItemCountInChest + " '" + Material.getMaterial(sellItemId) + "' for " + text + ".");
                return;
            } else {
                BCChatUtils.printInfo(player, ChatColor.RED, "This shop does not buy anything.");
                return;
            }
        }
    }
}
