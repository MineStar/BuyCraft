package de.minestar.buycraft.shops;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

import de.minestar.buycraft.manager.ShopManager;

public class ShopType {

    private final Sign sign;
    private final Chest chest;
    private final boolean shop;
    private final boolean userShop;
    private final boolean clickOnSign;

    public ShopType(ShopManager shopManager, Block block) {
        if (shopManager.isShopBlock(block)) {
            this.clickOnSign = shopManager.isWallSign(block);
            if (!this.clickOnSign) {
                this.chest = (Chest) block.getState();
                this.sign = (Sign) block.getRelative(BlockFace.UP).getState();
            } else {
                this.sign = (Sign) block.getState();
                this.chest = (Chest) block.getRelative(BlockFace.DOWN).getState();
            }
            this.userShop = !this.sign.getLine(0).equalsIgnoreCase("$SHOP$");
            this.shop = true;
        } else {
            this.sign = null;
            this.chest = null;
            this.userShop = false;
            this.shop = false;
            this.clickOnSign = false;
        }
    }

    public Sign getSign() {
        return sign;
    }

    public Chest getChest() {
        return chest;
    }

    public boolean isUserShop() {
        return userShop;
    }

    public boolean isInfiniteShop() {
        return !userShop;
    }

    public boolean isShop() {
        return shop;
    }

    public boolean isClickOnSign() {
        return clickOnSign;
    }
}
