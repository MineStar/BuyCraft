package com.bukkit.gemo.BuyCraft.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.BCShop;

public class DatabaseHandler {

    private DatabaseConnection dbConnection;

    // PREPARED STATEMENTS
    private PreparedStatement addUserShop;
    private PreparedStatement addItemsToShop;
    private PreparedStatement loadInventory;
    private PreparedStatement updateAccess;
    private PreparedStatement connectShopsToItem;
    private PreparedStatement getShop;
    private PreparedStatement getShopId;
    private PreparedStatement deleteSingleItem;
    private PreparedStatement deleteItemsFromShop;
    private PreparedStatement finishedShop;
    private PreparedStatement toggleShop;
    // /PREPARED STATEMENTS

    public DatabaseHandler() {
        try {
            createConnection();
            checkTables();
            initStatements();
        } catch (Exception e) {
            BCCore.printInConsole("Error while loading database!");
            e.printStackTrace();
        }
    }

    private void createConnection() throws Exception {
        FileConfiguration config = BCCore.getPlugin().getConfig();
        File f = new File(BCCore.getPlugin().getDataFolder(), "sqlconfig.yml");
        if (!f.exists()) {
            config.save(f);
            config.set("host", "localhost");
            config.set("port", "3306");
            config.set("database", "buycraft");
            config.set("user", "userName");
            config.set("password", "42");
            config.save(f);
            throw new FileNotFoundException(f.toString() + " was not found, a config file with default values was created!");
        }
        config.load(f);
        dbConnection = new DatabaseConnection(config.getString("host"), config.getInt("port"), config.getString("database"), config.getString("user"), config.getString("password"));
        BCCore.printInConsole("Connection to tatabase was sucessfully established!");
    }

    /**
     * Create tables when they are not existing. It does not prove whether the
     * tables has the correct structure!
     * 
     * @throws Exception
     */
    private void checkTables() throws Exception {
        Connection c = dbConnection.getConnection();
        //@formatter:off
        c.createStatement().execute(  
                "CREATE  TABLE IF NOT EXISTS `minestar_buycraft`.`Shop` (" +
                "`id` INT NOT NULL AUTO_INCREMENT ," +
                "`world` VARCHAR(45) NOT NULL ," +
                "`x` INT NOT NULL ," +
                "`y` INT NOT NULL ," +
                "`z` INT NOT NULL ," +
                "`active` TINYINT(1) NOT NULL ," +
                "`finished` TINYINT(1) NOT NULL ," +
                "`created` DATETIME NOT NULL ," +
                "`lastUsed` DATETIME NOT NULL ," +
                "PRIMARY KEY (`id`) )" +
                "ENGINE = InnoDB;");

        c.createStatement().execute(
                "CREATE  TABLE IF NOT EXISTS `minestar_buycraft`.`Items` (" +
                "`id` INT NOT NULL AUTO_INCREMENT ," +
                "`itemId` INT NOT NULL ," +
                "`subId` INT NOT NULL ," +
                "`amount` INT NOT NULL ," +
                "PRIMARY KEY (`id`) )" +
                "ENGINE = InnoDB;");

        c.createStatement().execute(
                "CREATE  TABLE IF NOT EXISTS `minestar_buycraft`.`Inventory` (" +
                "`Shop_id` INT NOT NULL ," +
                "`Items_id` INT NOT NULL ," +
                "PRIMARY KEY (`Shop_id`, `Items_id`) ," +
                "INDEX `fk_Shop_has_Items_Items1` (`Items_id` ASC) ," +
                "INDEX `fk_Shop_has_Items_Shop` (`Shop_id` ASC) ," +
                "CONSTRAINT `fk_Shop_has_Items_Shop`" +
                "  FOREIGN KEY (`Shop_id` )" +
                "  REFERENCES `minestar_buycraft`.`Shop` (`id` )" +
                "  ON DELETE NO ACTION" +
                "  ON UPDATE NO ACTION," +
                "CONSTRAINT `fk_Shop_has_Items_Items1`" +
                "  FOREIGN KEY (`Items_id` )" +
                "  REFERENCES `minestar_buycraft`.`Items` (`id` )" +
                "  ON DELETE NO ACTION" +
                "  ON UPDATE NO ACTION)" +
                "ENGINE = InnoDB;");
      //@formatter:on
    }

    public void closeConnection() {
        dbConnection.closeConnection();
    }

    private void initStatements() throws Exception {
        Connection con = dbConnection.getConnection();

        addUserShop = con.prepareStatement("INSERT INTO shop ( world, x, y, z, active, finished, created, lastUsed) VALUES ( ?, ?, ?, ?, ?, ?, NOW(), NOW())");
        // We need generated keys for the n:m connection
        addItemsToShop = con.prepareStatement("INSERT INTO items (itemId, subId, amount) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
        connectShopsToItem = con.prepareStatement("INSERT INTO inventory VALUES (?,?)");

        loadInventory = con.prepareStatement("SELECT itemId, subId, amount FROM items, shop, inventory WHERE shop.id = ? AND shop.id = inventory.Shop_id AND inventory.Item_id = items.id");
        getShop = con.prepareStatement("SELECT id, active, finished, DATE_FORMAT(created, '%d.%m.%Y'), DATE_FORMAT(lastUsed, '%d.%m.%Y') FROM shop WHERE x = ? AND y = ? and z = ? AND world = ?");
        getShopId = con.prepareStatement("SELECT id FROM shop WHERE x = ? AND y = ? AND z = ? AND worldName = ?");

        updateAccess = con.prepareStatement("UPDATE shop SET lastUsed = NOW() WHERE id = ?");
        finishedShop = con.prepareStatement("UPDATE shop SET finished = TRUE WHERE x = ? AND y = ? AND z = ? AND worldName = ?");
        toggleShop = con.prepareStatement("UPDATE shop SET active = !active WHERE x = ? AND y = ? AND z = ? AND worldName = ?");

        deleteItemsFromShop = con.prepareStatement("DELETE FROM items WHERE items.id = inventory.Item_id AND inventory.Shop_id = ?");
        deleteSingleItem = con.prepareStatement("DELETE FROM items WHERE itemId = ? AND subId = ? AND shop.id = ? AND items.id = inventory.Item_id AND shop.id = inventory.Shop_id", Statement.RETURN_GENERATED_KEYS);
    }

    public boolean addShop(int x, int y, int z, String worldName, boolean active, boolean finished) {

        // INSERT INTO shop ( world, x, y, z, active, finished, created,
        // lastUsed) VALUES ( ?, ?, ?, ?, ?, ?, NOW(), NOW())
        try {
            addUserShop.setInt(1, x);
            addUserShop.setInt(2, y);
            addUserShop.setInt(3, z);
            addUserShop.setString(4, worldName);
            addUserShop.setBoolean(5, active);
            addUserShop.setBoolean(6, finished);
            return addUserShop.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public BCShop loadShop(int x, int y, int z, String worldName) {
        // SELECT id, active, finished, DATE_FORMAT(created, '%d.%m.%Y'),
        // DATE_FORMAT(lastUsed, '%d.%m.%Y') FROM shop WHERE x = ? AND y = ? and
        // z = ? AND world = ?
        BCShop shop = null;
        try {
            getShop.setInt(1, x);
            getShop.setInt(2, y);
            getShop.setInt(3, z);
            getShop.setString(4, worldName);
            ResultSet rs = getShop.executeQuery();
            if (rs.next()) {
                // create new Shop ...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shop;
    }

    public boolean addItemsToShop(List<ItemStack> items, int x, int y, int z, String worldName) {
        // INSERT INTO items (itemId, subId, amount) VALUES (?,?,?)
        try {
            int shopId = getShopId(x, y, z, worldName);
            // There is no shop with the coordinates
            if (shopId == -1)
                return false;
            int itemId = 0;
            updateAccess(shopId);
            for (ItemStack item : items) {
                // add items to items table
                addItemsToShop.setInt(1, item.getTypeId());
                addItemsToShop.setInt(2, item.getDurability());
                addItemsToShop.setInt(3, item.getAmount());
                addItemsToShop.executeUpdate();
                // get generated primary key
                itemId = addItemsToShop.getGeneratedKeys().getInt(1);

                // connect item and shop
                connectShopsToItem.setInt(1, shopId);
                connectShopsToItem.setInt(2, itemId);
                connectShopsToItem.execute();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeItemFromShop(ItemStack item, int x, int y, int z, String worldName) {
        // DELETE FROM items WHERE itemId = ? AND subId = ? AND shop.id = ? AND
        // items.id = inventory.Item_id AND shop.id = inventory.Shop_id
        try {
            int shopId = getShopId(x, y, z, worldName);
            // There is no shop with the coordinates
            if (shopId == -1)
                return false;
            updateAccess(shopId);
            deleteSingleItem.setInt(1, item.getTypeId());
            deleteSingleItem.setInt(2, item.getDurability());
            deleteSingleItem.setInt(3, item.getAmount());
            deleteSingleItem.setInt(4, shopId);
            return deleteSingleItem.executeUpdate() != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getShopId(int x, int y, int z, String worldName) {
        // SELECT id FROM shop WHERE x = ? AND y = ? AND z = ? AND worldName = ?
        try {
            getShopId.setInt(1, x);
            getShopId.setInt(2, z);
            getShopId.setInt(3, y);
            getShopId.setString(4, worldName);
            ResultSet rs = getShopId.executeQuery();

            // No Shop with these coordinats exists
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean removeAllItems(int x, int y, int z, String worldName) {
        // DELETE FROM items WHERE items.id = inventory.Item_id AND
        // inventory.Shop_id = ?
        try {
            int shopId = getShopId(x, y, z, worldName);
            // There is no shop with the coordinates
            if (shopId == -1)
                return false;
            updateAccess(shopId);
            deleteItemsFromShop.setInt(1, shopId);
            return deleteItemsFromShop.executeUpdate() != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateAccess(int shopId) throws Exception {
        // UPDATE shop SET lastUsed = NOW() WHERE id = ?
        updateAccess.setInt(1, shopId);
        updateAccess.executeUpdate();
    }

    public List<ItemStack> loadItemsFromShop(int x, int y, int z, String worldName) {
        // SELECT itemId, subId, amount FROM items, shop, inventory WHERE
        // shop.id = ? AND shop.id = inventory.Shop_id AND inventory.Item_id =
        // items.id
        List<ItemStack> list = new LinkedList<ItemStack>();
        try {
            int shopId = getShopId(x, y, z, worldName);
            // There is no shop with the coordinates
            if (shopId == -1)
                return null;
            loadInventory.setInt(1, shopId);
            ResultSet rs = loadInventory.getResultSet();
            while (rs.next()) {
                // itemId, subId, amount
                list.add(new ItemStack(rs.getInt("itemId"), rs.getInt("amount"), rs.getShort("subId")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list.isEmpty() ? null : list;
    }

    public boolean toggleShop(int x, int y, int z, String worldName) {
        // UPDATE shop SET active = !active WHERE x = ? AND y = ? AND z = ? AND
        // worldName = ?
        try {
            toggleShop.setInt(1, x);
            toggleShop.setInt(2, y);
            toggleShop.setInt(3, z);
            toggleShop.setString(4, worldName);
            return toggleShop.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setFinished(int x, int y, int z, String worldName) {
        // UPDATE shop SET finished = TRUE WHERE x = ? AND y = ? AND z = ? AND
        // worldName = ?
        try {
            finishedShop.setInt(1, x);
            finishedShop.setInt(2, y);
            finishedShop.setInt(3, z);
            finishedShop.setString(4, worldName);
            return finishedShop.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
