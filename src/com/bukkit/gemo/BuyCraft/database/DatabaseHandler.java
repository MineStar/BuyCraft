package com.bukkit.gemo.BuyCraft.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;

import org.bukkit.configuration.file.FileConfiguration;

import com.bukkit.gemo.BuyCraft.BCCore;

public class DatabaseHandler {

    private DatabaseConnection dbConnection;

    // PREPARED STATEMENTS

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
                "`inventory` INT NOT NULL ," +
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
    }
}
