package com.bukkit.gemo.BuyCraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BCEntityListener implements Listener {
    BCCore plugin = null;

    // /////////////////////////////
    //
    // CONSTRUCTOR
    //
    // /////////////////////////////
    public BCEntityListener(BCCore plugin) {
        this.plugin = plugin;
    }

    // /////////////////////////////
    //
    // ON EXPLODE
    //
    // /////////////////////////////
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;

        for (Block block : event.blockList()) {
            if (BCBlockListener.isSignAnchor(block)) {
                event.setYield(0f);
                event.setCancelled(true);
                return;
            }

            if (block.getTypeId() == Material.CHEST.getId()) {
                if (BCBlockListener.hasRelativeSign(block) == 1) {
                    Sign sign = BCBlockListener.getRelativeSign(block);
                    if (sign.getLine(0).equalsIgnoreCase("$SHOP$")) {
                        /** CHECK INFINITE SHOP */
                        event.setYield(0f);
                        event.setCancelled(true);
                        return;
                    } else if (BCBlockListener.userShopList.containsKey(BCBlockListener.BlockToString(sign.getBlock()))) {
                        /** CHECK USERSHOP */
                        event.setYield(0f);
                        event.setCancelled(true);
                        return;
                    }
                }
            } else if (block.getTypeId() == Material.WALL_SIGN.getId()) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equalsIgnoreCase("$SHOP$")) {
                    /** CHECK INFINITE SHOP */
                    event.setYield(0f);
                    event.setCancelled(true);
                    return;

                } else if (BCBlockListener.userShopList.containsKey(BCBlockListener.BlockToString(sign.getBlock()))) {
                    /** CHECK USERSHOP */
                    event.setYield(0f);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}