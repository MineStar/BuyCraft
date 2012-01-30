package com.bukkit.gemo.BuyCraft;

import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bukkit.gemo.utils.UtilPermissions;

public class BCPlayerListener implements Listener {

    private TreeMap<String, MarketSelection> selections;

    // /////////////////////////////
    //
    // CONSTRUCTOR
    //
    // /////////////////////////////
    public BCPlayerListener(BCCore plugin) {
        selections = new TreeMap<String, MarketSelection>();
    }

    // /////////////////////////////
    //
    // ON INTERACT
    //
    // /////////////////////////////
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        // //////////////////////////////
        // HANDLE SELECTION
        // //////////////////////////////
        if (selections.containsKey(event.getPlayer().getName())) {
            event.getPlayer().sendMessage("SubID: " + event.getClickedBlock().getData());

            MarketSelection thisSelection = selections.get(event.getPlayer().getName());
            if (thisSelection.getCorner1() == null) {
                thisSelection.setCorner1(event.getClickedBlock().getLocation());
                BCChatUtils.printInfo(event.getPlayer(), ChatColor.GRAY, "Position 1 set.");
            } else if (thisSelection.getCorner1() != null && thisSelection.getCorner2() == null) {
                thisSelection.setCorner2(event.getClickedBlock().getLocation());
                BCChatUtils.printInfo(event.getPlayer(), ChatColor.GRAY, "Position 2 set.");
            } else if (thisSelection.getCorner1() != null && thisSelection.getCorner2() != null) {
                thisSelection.setCorner1(event.getClickedBlock().getLocation());
                thisSelection.setCorner2(null);
                BCChatUtils.printInfo(event.getPlayer(), ChatColor.GRAY, "Position 1 set and Position 2 cleared.");
            }
            selections.put(event.getPlayer().getName(), thisSelection);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }

        if (event.getClickedBlock().getTypeId() == Material.WALL_SIGN.getId()) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            if (sign == null)
                return;

            int shopType = -1;
            if (sign.getLine(0).equalsIgnoreCase("$SHOP$")) {
                // INFINITE SHOP
                shopType = 0;
            } else if (sign.getLine(0).startsWith("$") && sign.getLine(0).endsWith("$")) {
                // USER SHOP
                shopType = 1;
            }

            // NOT A SHOPSIGN
            if (shopType == -1)
                return;

            Player player = event.getPlayer();
            // //////////////////////////
            // CHECK PERMISSION
            // //////////////////////////
            if (shopType == 0) {
                /** INFINITE SHOP */
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.use")) {
                    BCChatUtils.printError(player, "Du darfst keine unendlichen Shops benutzen.");
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            } else if (shopType == 1) {
                /** USER SHOP */
                if (!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.use")) {
                    BCChatUtils.printError(player, "Du darfst keine Usershops benutzen.");
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            // //////////////////////////
            // SEARCH CHEST
            // //////////////////////////
            Block block = event.getClickedBlock();
            int chestCheck = BCBlockListener.hasRelativeChest(block);
            if (chestCheck != 1) {
                return;
            }

            Chest chest = BCBlockListener.getRelativeChest(block);

            // //////////////////////////
            // HANDLE CLICK
            // //////////////////////////
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
            }

            if (shopType == 0) {
                /** INFINITE SHOP */
                BCInfiniteShop shop = new BCInfiniteShop();
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
                    shop.handleRightClick(player, sign, chest);
                else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
                    shop.handleLeftClick(player, sign, chest);
                shop = null;
                return;
            } else if (shopType == 1) {
                /** USER SHOP */
                BCUserShop shop = BCBlockListener.userShopList.get(BCBlockListener.BlockToString(block));
                if (shop != null) {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
                        shop.handleRightClick(player, sign, chest);
                    else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
                        shop.handleLeftClick(player, sign, chest);
                    shop = null;
                } else {
                    BCChatUtils.printError(player, "An dieser Stelle wurde kein Usershop gefunden. Bitte gib einem Admin/Mod bescheid.");
                }
                return;
            }
        }

        // KLICK AUF DIE KISTE
        if (event.getClickedBlock().getTypeId() == Material.CHEST.getId()) {
            Block block = event.getClickedBlock();
            if (BCBlockListener.hasRelativeSign(block) == 1) {
                Sign sign = BCBlockListener.getRelativeSign(block);

                // IST USERSHOP?
                if (BCBlockListener.userShopList.containsKey(BCBlockListener.BlockToString(sign.getBlock()))) {
                    BCUserShop shop = BCBlockListener.userShopList.get(BCBlockListener.BlockToString(sign.getBlock()));
                    if (shop != null) {
                        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (!shop.isActive() && !BCCore.isShopOwner(event.getPlayer().getName(), BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$"))) {
                                BCChatUtils.printError(event.getPlayer(), "Dieser Shop ist momentan nicht aktiviert!");
                                BCChatUtils.printInfo(event.getPlayer(), ChatColor.GRAY, "Bitte gib dem Shop-Besitzer bescheid.");
                                event.setUseInteractedBlock(Event.Result.DENY);
                                event.setUseItemInHand(Event.Result.DENY);
                                event.setCancelled(true);
                            }
                        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            shop.handleLeftClick(event.getPlayer(), sign, (Chest) block.getState());
                        }
                    }
                }
                // IST INFINITE SHOP?
            }
        }
    }

    public TreeMap<String, MarketSelection> getSelections() {
        return selections;
    }
}