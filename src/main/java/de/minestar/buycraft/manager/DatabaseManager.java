package de.minestar.buycraft.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.bukkit.configuration.file.YamlConfiguration;

import de.minestar.buycraft.core.Core;
import de.minestar.buycraft.shops.UserShop;
import de.minestar.buycraft.units.BlockVector;
import de.minestar.buycraft.units.BuyCraftInventory;
import de.minestar.buycraft.units.BuyCraftStack;
import de.minestar.minestarlibrary.database.AbstractDatabaseHandler;
import de.minestar.minestarlibrary.database.DatabaseConnection;
import de.minestar.minestarlibrary.database.DatabaseType;
import de.minestar.minestarlibrary.database.DatabaseUtils;
import de.minestar.minestarlibrary.utils.ConsoleUtils;

public class DatabaseManager extends AbstractDatabaseHandler {

    private static DatabaseManager INSTANCE;

    private PreparedStatement addUsershop, removeUsershop, loadUsershops, getLastUsershop;
    private PreparedStatement getUsershopInventory, removeUsershopInventory;
    private PreparedStatement setShopFinished, setShopActive;
    private PreparedStatement addItemStack, getLastItemStack, updateItemStack, removeItemStack;

    public DatabaseManager(String pluginName, File dataFolder) {
        super(pluginName, dataFolder);
        INSTANCE = this;
    }

    @Override
    protected DatabaseConnection createConnection(String pluginName, File dataFolder) throws Exception {
        File configFile = new File(dataFolder, "sqlconfig.yml");
        YamlConfiguration config = new YamlConfiguration();

        if (!configFile.exists()) {
            DatabaseUtils.createDatabaseConfig(DatabaseType.SQLLite, configFile, pluginName);
            return null;
        }

        config.load(configFile);
        return new DatabaseConnection(pluginName, DatabaseType.SQLLite, config);
    }

    @Override
    protected void createStructure(String pluginName, Connection con) throws Exception {
        DatabaseUtils.createStructure(getClass().getResourceAsStream("/structure.sql"), con, pluginName);
    }

    @Override
    protected void createStatements(String pluginName, Connection con) throws Exception {
        this.addUsershop = con.prepareStatement("INSERT INTO tbl_usershops (isActive, shopFinished, creationTime, lastUsedTime, xPos, yPos, zPos, worldName) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        this.getLastUsershop = con.prepareStatement("SELECT * FROM tbl_usershops ORDER BY ID DESC LIMIT 1");

        this.addItemStack = con.prepareStatement("INSERT INTO tbl_usershops_inventories (ShopID, TypeID, SubID, Amount) VALUES (?, ?, ?, ?)");
        this.getLastItemStack = con.prepareStatement("SELECT * FROM tbl_usershops_inventories ORDER BY ID DESC LIMIT 1");
        this.updateItemStack = con.prepareStatement("UPDATE tbl_usershops_inventories SET Amount = ? WHERE ID = ?");
        this.removeItemStack = con.prepareStatement("DELETE FROM tbl_usershops_inventories WHERE ID = ?");

        this.removeUsershop = con.prepareStatement("DELETE FROM tbl_usershops WHERE ID = ?");
        this.removeUsershopInventory = con.prepareStatement("DELETE FROM tbl_usershops_inventories WHERE ShopID = ?");

        this.loadUsershops = con.prepareStatement("SELECT * FROM tbl_usershops ORDER BY id ASC");
        this.getUsershopInventory = con.prepareStatement("SELECT * FROM tbl_usershops_inventories WHERE ShopID = ?");

        this.setShopFinished = con.prepareStatement("UPDATE tbl_usershops SET shopFinished = ? WHERE ID = ?");
        this.setShopActive = con.prepareStatement("UPDATE tbl_usershops SET isActive = ? WHERE ID = ?");
    }

    public BuyCraftStack createItemStack(UserShop shop, int TypeID, short SubID, int Amount) {
        try {
            this.addItemStack.setInt(1, shop.getShopID());
            this.addItemStack.setInt(2, TypeID);
            this.addItemStack.setInt(3, SubID);
            this.addItemStack.setInt(4, Amount);
            this.addItemStack.executeUpdate();
            BuyCraftStack stack = this.getLastItemStack();
            if (stack == null) {
                return null;
            }
            return stack;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't save new itemstack in database!");
            return null;
        }
    }

    public boolean updateItemStack(BuyCraftStack stack) {
        try {
            this.updateItemStack.setInt(1, stack.getAmount());
            this.updateItemStack.setInt(2, stack.getStackID());
            this.updateItemStack.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't modify ItemStack (Amount) in database! ID=" + stack.getStackID());
            return false;
        }
    }

    public boolean removeItemStack(BuyCraftStack stack) {
        try {
            this.removeItemStack.setInt(1, stack.getStackID());
            this.removeItemStack.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't remove ItemStack from database! ID=" + stack.getStackID());
            return false;
        }
    }

    private BuyCraftStack getLastItemStack() {
        try {
            ResultSet results = this.getLastItemStack.executeQuery();
            while (results.next()) {
                return new BuyCraftStack(results);
            }
            return null;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't get last itemstack from database!");
            return null;
        }
    }

    public UserShop addUsershop(BlockVector position) {
        try {
            this.addUsershop.setBoolean(1, false);
            this.addUsershop.setBoolean(2, false);
            this.addUsershop.setLong(3, System.currentTimeMillis());
            this.addUsershop.setLong(4, System.currentTimeMillis());
            this.addUsershop.setInt(5, position.getX());
            this.addUsershop.setInt(6, position.getY());
            this.addUsershop.setInt(7, position.getZ());
            this.addUsershop.setString(8, position.getWorldName());
            this.addUsershop.executeUpdate();
            UserShop newShop = this.getLastUsershop();
            if (newShop == null) {
                return null;
            }
            if (!newShop.getPosition().equals(position)) {
                return null;
            }
            return newShop;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't save new usershop in database!");
            return null;
        }
    }

    private UserShop getLastUsershop() {
        try {
            ResultSet results = this.getLastUsershop.executeQuery();
            while (results.next()) {
                return new UserShop(results);
            }
            return null;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't get last usershop from database!");
            return null;
        }
    }

    /**
     * Load all usershops from the database
     * 
     * @return
     */
    public ArrayList<UserShop> loadUsershops() {
        ArrayList<UserShop> list = new ArrayList<UserShop>();
        try {
            // GET SHOPS FROM DB
            ResultSet results = this.loadUsershops.executeQuery();
            while (results.next()) {
                list.add(new UserShop(results));
            }

            // GET INVENTORIES FROM DB
            for (UserShop shop : list) {
                BuyCraftInventory inventory = this.getInventory(shop.getShopID());
                if (inventory != null) {
                    shop.setInventory(inventory);
                }
            }
            ConsoleUtils.printInfo(Core.NAME, "Usershops loaded: " + list.size());
            return list;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't load usershops from database!");
            return null;
        }
    }

    /**
     * Load the inventory for a usershops from the database
     * 
     * @param ID
     * @return the BuyCraftInventory
     */
    private BuyCraftInventory getInventory(int ID) {
        try {
            BuyCraftInventory inventory = new BuyCraftInventory();
            this.getUsershopInventory.setInt(1, ID);
            ResultSet results = this.getUsershopInventory.executeQuery();
            while (results.next()) {
                BuyCraftStack stack = new BuyCraftStack(results.getInt(1), results.getInt(3), results.getShort(4), results.getInt(5));
                inventory.addItem(stack);
            }
            return inventory;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't load inventory for ShopID=" + ID + " from database!");
            return null;
        }
    }

    /**
     * Set a Usershop active/inactive
     * 
     * @param shop
     * @param active
     * @return <b>true</b> if the update succeeded, otherwise <b>false</b>
     */
    public boolean setUsershopActive(UserShop shop, boolean active) {
        try {
            this.setShopActive.setBoolean(1, active);
            this.setShopActive.setInt(2, shop.getShopID());
            this.setShopActive.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't modify Shop (isActive) in database! ID=" + shop.getShopID());
            return false;
        }
    }

    /**
     * Set a Usershop finished/unfinished
     * 
     * @param shop
     * @param active
     * @return <b>true</b> if the update succeeded, otherwise <b>false</b>
     */
    public boolean setUsershopFinished(UserShop shop, boolean finished) {
        try {
            this.setShopFinished.setBoolean(1, finished);
            this.setShopFinished.setInt(2, shop.getShopID());
            this.setShopFinished.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't modify Shop (shopFinished) in database! ID=" + shop.getShopID());
            return false;
        }
    }

    /**
     * Remove a usershop (and its inventory) from the database
     * 
     * @param shop
     * @return <b>true</b> if the update succeeded without errors, otherwise
     *         <b>false</b>
     */
    public boolean removeUsershop(UserShop shop) {
        boolean success = true;
        try {
            this.removeUsershop.setInt(1, shop.getShopID());
            this.removeUsershop.executeUpdate();
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't delete usershop from database! ID=" + shop.getShopID());
            success = false;
        }
        success = this.removeInventory(shop);
        return success;
    }

    public boolean removeInventory(UserShop shop) {
        try {
            this.removeUsershopInventory.setInt(1, shop.getShopID());
            this.removeUsershopInventory.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't delete usershop-inventory from database! ID=" + shop.getShopID());
            return false;
        }
    }

    /**
     * STATIC METHOD THE GET THE INSTANCE
     * 
     * @return the DatabaseManager-Instance
     */
    public static DatabaseManager getInstance() {
        return INSTANCE;
    }
}
