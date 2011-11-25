package com.bukkit.gemo.BuyCraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class BCEntityListener extends EntityListener {
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
    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
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