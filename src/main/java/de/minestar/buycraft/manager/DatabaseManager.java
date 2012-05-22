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
import de.minestar.buycraft.units.PersistentBuyCraftStack;
import de.minestar.buycraft.units.PersistentAlias;
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
    private PreparedStatement addAlias, loadAliases, getLastAlias, removeAlias;

    public DatabaseManager(String pluginName, File dataFolder) {
        super(pluginName, dataFolder);
        INSTANCE = this;
    }

    @Override
    protected DatabaseConnection createConnection(String pluginName, File dataFolder) throws Exception {
        File configFile = new File(dataFolder, "sqlconfig.yml");
        YamlConfiguration config = new YamlConfiguration();

        if (!configFile.exists()) {
            DatabaseUtils.createDatabaseConfig(DatabaseType.MySQL, configFile, pluginName);
            return null;
        }

        config.load(configFile);
        return new DatabaseConnection(pluginName, DatabaseType.MySQL, config);
    }

    @Override
    protected void createStructure(String pluginName, Connection con) throws Exception {
        DatabaseUtils.createStructure(getClass().getResourceAsStream("/structure.sql"), con, pluginName);
    }

    @Override
    protected void createStatements(String pluginName, Connection con) throws Exception {
        // USERSHOPS
        this.addUsershop = con.prepareStatement("INSERT INTO tbl_usershops (isActive, shopFinished, creationTime, lastUsedTime, xPos, yPos, zPos, worldName) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        this.getLastUsershop = con.prepareStatement("SELECT * FROM tbl_usershops ORDER BY ID DESC LIMIT 1");

        this.loadUsershops = con.prepareStatement("SELECT * FROM tbl_usershops ORDER BY id ASC");
        this.removeUsershop = con.prepareStatement("DELETE FROM tbl_usershops WHERE ID = ?");

        // INVENTORIES
        this.addItemStack = con.prepareStatement("INSERT INTO tbl_usershops_inventories (ShopID, TypeID, SubID, Amount) VALUES (?, ?, ?, ?)");
        this.getLastItemStack = con.prepareStatement("SELECT * FROM tbl_usershops_inventories ORDER BY ID DESC LIMIT 1");
        this.getUsershopInventory = con.prepareStatement("SELECT * FROM tbl_usershops_inventories WHERE ShopID = ?");

        this.updateItemStack = con.prepareStatement("UPDATE tbl_usershops_inventories SET Amount = ? WHERE ID = ?");
        this.removeItemStack = con.prepareStatement("DELETE FROM tbl_usershops_inventories WHERE ID = ?");

        this.removeUsershopInventory = con.prepareStatement("DELETE FROM tbl_usershops_inventories WHERE ShopID = ?");

        // CHANGE SHOP-PROPERTIES
        this.setShopFinished = con.prepareStatement("UPDATE tbl_usershops SET shopFinished = ? WHERE ID = ?");
        this.setShopActive = con.prepareStatement("UPDATE tbl_usershops SET isActive = ? WHERE ID = ?");

        // ALIASES
        this.addAlias = con.prepareStatement("INSERT INTO tbl_aliases (playerName, aliasName) VALUES (?, ?)");
        this.loadAliases = con.prepareStatement("SELECT * FROM tbl_aliases");
        this.removeAlias = con.prepareStatement("DELETE FROM tbl_aliases WHERE ID = ?");
        this.getLastAlias = con.prepareStatement("SELECT * FROM tbl_aliases ORDER BY ID DESC LIMIT 1");
    }

    /**
     * Create a new BuyCraftStack
     * 
     * @param shop
     * @param TypeID
     * @param SubID
     * @param Amount
     * @return the BuyCraftStack
     */
    public PersistentBuyCraftStack createItemStack(UserShop shop, int TypeID, short SubID, int Amount) {
        try {
            this.addItemStack.setInt(1, shop.getShopID());
            this.addItemStack.setInt(2, TypeID);
            this.addItemStack.setInt(3, SubID);
            this.addItemStack.setInt(4, Amount);
            this.addItemStack.executeUpdate();
            PersistentBuyCraftStack stack = this.getLastItemStack();
            if (stack == null) {
                return null;
            }
            return stack;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't save new itemstack in database!");
            return null;
        }
    }

    /**
     * Update a BuyCraftStack in the database
     * 
     * @param stack
     * @return <b>true</b> if the update succeeded, otherwise <b>false</b>
     */
    public boolean updateItemStack(PersistentBuyCraftStack stack) {
        try {
            this.updateItemStack.setInt(1, stack.getAmount());
            this.updateItemStack.setInt(2, stack.getStackID());
            this.updateItemStack.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't modify ItemStack (Amount) in database! " + stack.toString());
            return false;
        }
    }

    /**
     * Delete a single BuyCraftStack from the database
     * 
     * @param stack
     * @return <b>true</b> if deletion succeeded, otherwise <b>false</b>
     */
    public boolean removeItemStack(PersistentBuyCraftStack stack) {
        try {
            this.removeItemStack.setInt(1, stack.getStackID());
            this.removeItemStack.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't remove ItemStack from database! " + stack.toString());
            return false;
        }
    }

    /**
     * Get the last created BuyCraftStack
     * 
     * @return the BuyCraftStack
     */
    private PersistentBuyCraftStack getLastItemStack() {
        try {
            ResultSet results = this.getLastItemStack.executeQuery();
            while (results.next()) {
                return new PersistentBuyCraftStack(results);
            }
            return null;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't get last itemstack from database!");
            return null;
        }
    }

    /**
     * Create a Usershop
     * 
     * @param position
     * @return the Usershop
     */
    public UserShop createUsershop(BlockVector position) {
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

    /**
     * Get the last created Usershop
     * 
     * @return the Usershop
     */
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
     * @return a list of usershops (with inventories)
     */
    public ArrayList<UserShop> loadUsershops() {
        ArrayList<UserShop> list = new ArrayList<UserShop>();
        try {
            // GET SHOPS FROM DB
            ResultSet results = this.loadUsershops.executeQuery();
            ArrayList<UserShop> notValidList = new ArrayList<UserShop>();
            while (results.next()) {
                UserShop shop = new UserShop(results);
                if (shop.isValid())
                    list.add(shop);
                else
                    notValidList.add(shop);
            }

            // GET INVENTORIES FROM DB
            for (UserShop shop : list) {
                BuyCraftInventory inventory = this.getInventory(shop.getShopID());
                if (inventory != null) {
                    shop.setInventory(inventory);
                }
            }

            ConsoleUtils.printInfo(Core.NAME, "Usershops loaded: " + list.size());
            if (notValidList.size() > 0) {
                ConsoleUtils.printError(Core.NAME, "Usershops NOT loaded: " + notValidList.size());
                int i = 1;
                for (UserShop shop : notValidList) {
                    ConsoleUtils.printInfo(Core.NAME, "#" + i + " : " + shop.getPosition().toString());
                    ++i;
                }
            }
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
                PersistentBuyCraftStack stack = new PersistentBuyCraftStack(results);
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

    /**
     * Remove an inventory for a Usershop
     * 
     * @param shop
     * @return <b>true</b> if deletion succeeded, otherwise <b>false</b>
     */
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
     * Create a new alias in the database
     * 
     * @param playerName
     * @param aliasName
     * @return the alias
     */
    public PersistentAlias createAlias(String playerName, String aliasName) {
        try {
            this.addAlias.setString(1, playerName);
            this.addAlias.setString(2, aliasName);
            this.addAlias.executeUpdate();
            PersistentAlias alias = this.getLastAlias();
            if (alias == null) {
                return null;
            }
            return alias;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't save new alias in database!");
            return null;
        }
    }

    /**
     * Get the last created alias
     * 
     * @return the alias
     */
    private PersistentAlias getLastAlias() {
        try {
            ResultSet results = this.getLastAlias.executeQuery();
            while (results.next()) {
                return new PersistentAlias(results);
            }
            return null;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't get last alias from database!");
            return null;
        }
    }

    /**
     * Load all aliases from the database
     * 
     * @return a list of aliases
     */
    public ArrayList<PersistentAlias> loadAliases() {
        ArrayList<PersistentAlias> list = new ArrayList<PersistentAlias>();
        try {
            // GET SHOPS FROM DB
            ResultSet results = this.loadAliases.executeQuery();
            while (results.next()) {
                list.add(new PersistentAlias(results));
            }
            ConsoleUtils.printInfo(Core.NAME, "Aliases loaded: " + list.size());
            return list;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't load aliases from database!");
            return null;
        }
    }

    /**
     * Remove an alias from the database
     * 
     * @param alias
     * @return <b>true</b> if deletion succeeded, otherwise <b>false</b>
     */
    public boolean removeAlias(PersistentAlias alias) {
        try {
            this.removeAlias.setInt(1, alias.getID());
            this.removeAlias.executeUpdate();
            return true;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't delete alias from database! " + alias.toString());
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
