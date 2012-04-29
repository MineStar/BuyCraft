package de.minestar.buycraft.manager;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class ShopManager {

    public Sign getSignAnchor(Block block) {
        if (this.isWallSign(block.getRelative(+1, 0, 0)) && block.getRelative(+1, 0, 0).getData() == 5) {
            Sign sign = (Sign) block.getRelative(+1, 0, 0).getState();
            if (this.isShop(sign.getLines())) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(-1, 0, 0)) && block.getRelative(-1, 0, 0).getData() == 4) {
            Sign sign = (Sign) block.getRelative(-1, 0, 0).getState();
            if (this.isShop(sign.getLines())) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(0, 0, +1)) && block.getRelative(0, 0, +1).getData() == 3) {
            Sign sign = (Sign) block.getRelative(0, 0, +1).getState();
            if (this.isShop(sign.getLines())) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(0, 0, -1)) && block.getRelative(0, 0, -1).getData() == 2) {
            Sign sign = (Sign) block.getRelative(0, 0, -1).getState();
            if (this.isShop(sign.getLines())) {
                return sign;
            }
        }
        return null;
    }

    public Sign getSignAnchor(List<Block> blockList) {
        Sign sign;
        for (Block block : blockList) {
            if ((sign = this.getSignAnchor(block)) != null) {
                return sign;
            }
        }
        return null;
    }

    /**
     * Check if a block is a chest
     * 
     * @param block
     * @return <b>true</b> if the block is a chest, otherwise <b>false</b>.
     */
    public boolean isChest(Block block) {
        return block.getTypeId() == Material.CHEST.getId();
    }

    /**
     * Check if a block is a wallsign
     * 
     * @param block
     * @return <b>true</b> if the block is a wallsign, otherwise <b>false</b>.
     */
    public boolean isWallSign(Block block) {
        return block.getTypeId() == Material.WALL_SIGN.getId();
    }

    /**
     * Check if a sign is a infinite-shopsign
     * 
     * @param lines
     * @return <b>true</b> if the block is a infinite-shopsign, otherwise
     *         <b>false</b>.
     */
    public boolean isInfiniteShop(String[] lines) {
        if (lines[0].equalsIgnoreCase("$SHOP$"))
            return true;
        return false;
    }

    /**
     * Check if a sign is a user-shopsign
     * 
     * @param lines
     * @return <b>true</b> if the block is a user-shopsign, otherwise
     *         <b>false</b>.
     */
    public boolean isUserShop(String[] lines) {
        // TODO: Implement Usershops
        return false;
    }

    /**
     * Check if a sign is a shopsign
     * 
     * @param lines
     * @return <b>true</b> if the block is a shopsign, otherwise <b>false</b>.
     */
    private boolean isShop(String[] lines) {
        return this.isInfiniteShop(lines) || this.isUserShop(lines);
    }

    /**
     * Is at least one block a shopblock? Used for PlayerInteract. It will only
     * search for a sign and a chest.
     * 
     * @param block
     * @return <b>true</b> if the block is used for a shop, otherwise
     *         <b>false</b>.
     */
    public boolean isShopBlock(List<Block> blockList) {
        for (Block block : blockList) {
            // Is it a shopsign? (with a chest below it)
            if (this.isWallSign(block)) {
                Sign sign = (Sign) block.getState();
                if (this.isShop(sign.getLines())) {
                    Block relative = block.getRelative(BlockFace.DOWN);
                    return this.isChest(relative);
                }
            }
            // Is it a chest? (with a shopsign above it)
            else if (this.isChest(block)) {
                Block relative = block.getRelative(BlockFace.UP);
                if (this.isWallSign(relative)) {
                    Sign sign = (Sign) relative.getState();
                    return this.isShop(sign.getLines());
                }
            }
        }
        return false;
    }

    /**
     * Is a block a shopblock? Used for PlayerInteract. It will only search for
     * a sign and a chest.
     * 
     * @param block
     * @return <b>true</b> if the block is used for a shop, otherwise
     *         <b>false</b>.
     */
    public boolean isShopBlock(Block block) {
        // Is it a shopsign? (with a chest below it)
        if (this.isWallSign(block)) {
            Sign sign = (Sign) block.getState();
            if (this.isShop(sign.getLines())) {
                Block relative = block.getRelative(BlockFace.DOWN);
                return this.isChest(relative);
            }
            return false;
        }
        // Is it a chest? (with a shopsign above it)
        if (this.isChest(block)) {
            Block relative = block.getRelative(BlockFace.UP);
            if (this.isWallSign(relative)) {
                Sign sign = (Sign) relative.getState();
                return this.isShop(sign.getLines());
            }
            return false;
        }
        return false;
    }
}
