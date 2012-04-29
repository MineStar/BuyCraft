package de.minestar.buycraft.listener;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;

import de.minestar.buycraft.core.Core;
import de.minestar.buycraft.core.Messages;
import de.minestar.buycraft.core.Permission;
import de.minestar.buycraft.manager.ItemManager;
import de.minestar.buycraft.manager.ShopManager;
import de.minestar.buycraft.shops.InfiniteShop;
import de.minestar.buycraft.shops.ShopType;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class ActionListener implements Listener {

    private ShopManager shopManager;
    private ItemManager itemManager;

    public ActionListener(ShopManager shopManager, ItemManager itemManager) {
        this.shopManager = shopManager;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;

        // clicked on air => return
        if (!event.hasBlock())
            return;

        // Get the shoptype
        ShopType shop = new ShopType(this.shopManager, event.getClickedBlock());
        // not a shop-sign => return;
        if (!shop.isShop()) {
            return;
        }

        /** HANDLE SHOPS */
        if (shop.isInfiniteShop()) {
            // Handle infiniteshop
            // check permissions, if clicked on a sign
            if (shop.isClickOnSign() && !UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
            }

            if (shop.isClickOnSign()) {
                if (shop.getSign().getLine(1).length() < 1) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setCancelled(true);
                    InfiniteShop.activate(event, shop);
                    return;
                }
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    InfiniteShop.handleSignInteract(shop, event.getPlayer());
                } else {
                    InfiniteShop.handleChestInteract(shop, event.getPlayer());
                }
            } else {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    InfiniteShop.handleChestInteract(shop, event.getPlayer());
                }
            }
        } else {
            /** Handle Usershop */
            // TODO: Handle usershops
        }
    }
    // //////////////////////////////////////////////
    //
    // BLOCK-EVENTS
    //
    // //////////////////////////////////////////////

    private void handleSignBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (this.shopManager.isWallSign(event.getBlock())) {
            Sign sign = (Sign) event.getBlock().getState();
            // handle infinite-shop
            if (this.shopManager.isInfiniteShop(sign.getLines())) {
                if (!UtilPermissions.playerCanUseCommand(player, Permission.INFINITE_SHOP_CREATE)) {
                    PlayerUtils.sendError(player, Core.NAME, Messages.INFINITE_SHOP_DESTROY_ERROR);
                    event.setCancelled(true);
                } else {
                    PlayerUtils.sendSuccess(player, Core.NAME, Messages.INFINITE_SHOP_DESTROY_SUCCESS);
                }
            }
            // handle usershop
            else if (this.shopManager.isUserShop(sign.getLines())) {
                // TODO: HANDLE USERSHOPS
            }
        } else if (this.shopManager.isChest(event.getBlock())) {
            if (!UtilPermissions.playerCanUseCommand(player, Permission.INFINITE_SHOP_CREATE)) {
                PlayerUtils.sendError(player, Core.NAME, Messages.INFINITE_SHOP_DESTROY_ERROR);
                event.setCancelled(true);
            }
        }
    }

    private void handleAnchorBreak(BlockBreakEvent event) {
        PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.DESTROY_SIGN_FIRST);
        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled())
            return;

        if (this.shopManager.isInfiniteShop(event.getLines())) {
            // check permissions
            if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE)) {
                PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.INFINITE_SHOP_CREATE_ERROR);
                SignUtils.cancelSignCreation(event);
                return;
            }

            event.setLine(0, "$SHOP$");
            int[] buyRatios = ItemManager.getRatio(event.getLine(2));
            int[] sellRatios = ItemManager.getRatio(event.getLine(3));

            // all ratios == 0? => error
            if (buyRatios[0] <= 0 && buyRatios[1] <= 0 && sellRatios[0] <= 0 && sellRatios[1] <= 0) {
                PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.WRONG_SYNTAX);
                SignUtils.cancelSignCreation(event);
                return;
            }

            // check line 2
            if (event.getLine(1).length() > 0) {
                String[] split = ItemManager.extractItemLine(event.getLine(1)).split(":");
                int TypeID = this.itemManager.getItemId(split[0]);
                short data = 0;
                if (split.length == 2) {
                    try {
                        data = Short.valueOf(split[1]);
                    } catch (Exception e) {
                        data = 0;
                    }
                }
                if (!this.itemManager.isItemIDAllowed(TypeID)) {
                    PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.ITEM_NOT_ALLOWED);
                    SignUtils.cancelSignCreation(event);
                    return;
                }
                String line = this.itemManager.getItemName(TypeID);
                if (data > 0) {
                    line += ":" + data;
                }
                event.setLine(1, line);
            }

            PlayerUtils.sendSuccess(event.getPlayer(), Core.NAME, Messages.INFINITE_SHOP_CREATE_SUCCESS);
            if (event.getLine(1).length() < 1) {
                PlayerUtils.sendMessage(event.getPlayer(), ChatColor.GRAY, Core.NAME, Messages.INFINITE_SHOP_CREATE_INFO);
            }
        } else if (this.shopManager.isUserShop(event.getLines())) {
            // TODO: Implement usershops
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        /** IS THE BLOCK A SHOP-SIGN? */
        if (this.shopManager.isShopBlock(event.getBlock())) {
            this.handleSignBreak(event);
            return;
        }
        /** IS THE BLOCK A SIGN-ANCHOR? */
        else if (this.shopManager.getSignAnchor(event.getBlock()) != null) {
            this.handleAnchorBreak(event);
            return;
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.isCancelled())
            return;

        // not a shop-sign => return;
        if (this.shopManager.isShopBlock(event.getBlock()) || (this.shopManager.getSignAnchor(event.getBlock()) != null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;

        // not a shop-sign => return;
        if (this.shopManager.isShopBlock(event.getBlocks()) || (this.shopManager.getSignAnchor(event.getBlocks()) != null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;

        // not a sticky piston => return;
        if (!event.isSticky())
            return;

        // not a shop-sign => return;
        if (this.shopManager.isShopBlock(event.getRetractLocation().getBlock()) || (this.shopManager.getSignAnchor(event.getRetractLocation().getBlock()) != null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;

        // not a shop-sign => return;
        if (this.shopManager.isShopBlock(event.blockList()) || (this.shopManager.getSignAnchor(event.blockList()) != null)) {
            event.setYield(0.0f);
            event.setCancelled(true);
        }
    }
}
