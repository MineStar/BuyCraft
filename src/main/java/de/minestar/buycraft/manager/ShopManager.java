package de.minestar.buycraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import de.minestar.buycraft.shops.UserShop;
import de.minestar.buycraft.units.Alias;
import de.minestar.buycraft.units.BlockVector;
import de.minestar.buycraft.units.PersistentAlias;

public class ShopManager {

    private DatabaseManager databaseManager;
    private HashMap<BlockVector, UserShop> usershops;
    private HashMap<String, PersistentAlias> aliasesByPlayerName, aliasesByAliasName;
    private boolean loadSucceeded = false;

    public ShopManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.usershops = new HashMap<BlockVector, UserShop>();
        this.aliasesByPlayerName = new HashMap<String, PersistentAlias>();
        this.aliasesByAliasName = new HashMap<String, PersistentAlias>();
        this.loadUsershops();
        this.loadAliases();
    }

    public Sign getSignAnchor(Block block) {
        BlockVector position = new BlockVector(block.getLocation());
        if (this.isWallSign(block.getRelative(+1, 0, 0)) && block.getRelative(+1, 0, 0).getData() == 5) {
            Sign sign = (Sign) block.getRelative(+1, 0, 0).getState();
            if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(-1, 0, 0)) && block.getRelative(-1, 0, 0).getData() == 4) {
            Sign sign = (Sign) block.getRelative(-1, 0, 0).getState();
            if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(0, 0, +1)) && block.getRelative(0, 0, +1).getData() == 3) {
            Sign sign = (Sign) block.getRelative(0, 0, +1).getState();
            if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
                return sign;
            }
        }
        if (this.isWallSign(block.getRelative(0, 0, -1)) && block.getRelative(0, 0, -1).getData() == 2) {
            Sign sign = (Sign) block.getRelative(0, 0, -1).getState();
            if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
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
     * @return <b>true</b> if the block is a infinite-shopsign, otherwise <b>false</b>.
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
     * @return <b>true</b> if the block is a usershop, otherwise <b>false</b>.
     */
    public boolean isUserShop(BlockVector position) {
        return this.usershops.containsKey(position);
    }

    /**
     * Get a usershop based on the position
     * 
     * @param position
     * @return the usershop
     */
    public UserShop getUserShop(BlockVector position) {
        return this.usershops.get(position);
    }

    /**
     * Check if a sign is a shopsign
     * 
     * @param lines
     * @return <b>true</b> if the block is a shopsign, otherwise <b>false</b>.
     */
    private boolean isShop(String[] lines, BlockVector position) {
        return this.isInfiniteShop(lines) || this.isUserShop(position);
    }

    /**
     * Is at least one block a shopblock? Used for PlayerInteract. It will only search for a sign and a chest.
     * 
     * @param block
     * @return <b>true</b> if the block is used for a shop, otherwise <b>false</b>.
     */
    public boolean isShopBlock(List<Block> blockList) {
        for (Block block : blockList) {
            // Is it a shopsign? (with a chest below it)
            if (this.isWallSign(block)) {
                Sign sign = (Sign) block.getState();
                if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
                    Block relative = block.getRelative(BlockFace.DOWN);
                    return this.isChest(relative);
                }
            }
            // Is it a chest? (with a shopsign above it)
            else if (this.isChest(block)) {
                Block relative = block.getRelative(BlockFace.UP);
                if (this.isWallSign(relative)) {
                    Sign sign = (Sign) relative.getState();
                    return this.isShop(sign.getLines(), new BlockVector(sign.getLocation()));
                }
            }
        }
        return false;
    }

    /**
     * Is a block a shopblock? Used for PlayerInteract. It will only search for a sign and a chest.
     * 
     * @param block
     * @return <b>true</b> if the block is used for a shop, otherwise <b>false</b>.
     */
    public boolean isShopBlock(Block block) {
        // Is it a shopsign? (with a chest below it)
        if (this.isWallSign(block)) {
            Sign sign = (Sign) block.getState();
            if (this.isShop(sign.getLines(), new BlockVector(sign.getLocation()))) {
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
                return this.isShop(sign.getLines(), new BlockVector(sign.getLocation()));
            }
            return false;
        }
        return false;
    }

    private void loadUsershops() {
        ArrayList<UserShop> shopList = this.databaseManager.loadUsershops();
        this.loadSucceeded = (shopList != null);
        if (this.loadSucceeded) {
            for (UserShop shop : shopList) {
                this.usershops.put(shop.getPosition(), shop);
            }
        }
    }

    public UserShop addUsershop(BlockVector position) {
        if (this.getUserShop(position) != null)
            return null;

        UserShop newShop = this.databaseManager.createUsershop(position);
        if (newShop != null) {
            this.usershops.put(position, newShop);
        }
        return newShop;
    }

    public boolean removeUsershop(UserShop shop) {
        boolean result = this.databaseManager.removeUsershop(shop);
        if (result) {
            this.usershops.remove(shop.getPosition());
        }
        return result;
    }

    public PersistentAlias addAlias(String playerName, String aliasName) {
        if (this.getPersistentAlias(playerName) != null)
            return null;

        PersistentAlias alias = this.databaseManager.createAlias(playerName, aliasName);
        if (alias != null) {
            this.aliasesByAliasName.put(alias.getAliasName().toLowerCase(), alias);
            this.aliasesByPlayerName.put(alias.getPlayerName().toLowerCase(), alias);
        }
        return alias;
    }

    public Alias getAlias(String name) {
        Alias alias = this.aliasesByPlayerName.get(name.toLowerCase());
        if (alias == null) {
            alias = this.aliasesByAliasName.get(name.toLowerCase());
            if (alias == null) {
                return new Alias(name, name);
            }
        }
        return alias;
    }

    public PersistentAlias getPersistentAlias(String name) {
        PersistentAlias alias = this.aliasesByPlayerName.get(name.toLowerCase());
        if (alias == null) {
            return this.aliasesByAliasName.get(name.toLowerCase());
        }
        return alias;
    }

    public boolean removeAlias(PersistentAlias alias) {
        boolean result = this.databaseManager.removeAlias(alias);
        if (result) {
            this.aliasesByAliasName.remove(alias.getAliasName().toLowerCase());
            this.aliasesByPlayerName.remove(alias.getPlayerName().toLowerCase());
        }
        return result;
    }

    public ArrayList<PersistentAlias> getAllAliases() {
        return this.databaseManager.loadAliases();
    }

    private void loadAliases() {
        ArrayList<PersistentAlias> aliasList = this.databaseManager.loadAliases();
        this.loadSucceeded = (aliasList != null);
        if (this.loadSucceeded) {
            for (PersistentAlias alias : aliasList) {
                this.aliasesByAliasName.put(alias.getAliasName().toLowerCase(), alias);
                this.aliasesByPlayerName.put(alias.getPlayerName().toLowerCase(), alias);
            }
        }
    }
}
