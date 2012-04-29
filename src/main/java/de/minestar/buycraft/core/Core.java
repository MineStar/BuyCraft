package de.minestar.buycraft.core;

import org.bukkit.plugin.PluginManager;

import de.minestar.buycraft.listener.ActionListener;
import de.minestar.buycraft.manager.ItemManager;
import de.minestar.buycraft.manager.ShopManager;
import de.minestar.minestarlibrary.AbstractCore;

public class Core extends AbstractCore {
    public static final String NAME = "BuyCraft";

    /**
     * Manager
     */
    private ShopManager shopManager;
    private ItemManager itemManager;

    /**
     * Listener
     */
    private ActionListener guardListener;

    public Core() {
        this(NAME);
    }

    public Core(String name) {
        super(NAME);
    }

    @Override
    protected boolean createManager() {
        this.shopManager = new ShopManager();
        this.itemManager = new ItemManager();
        return true;
    }

    @Override
    protected boolean createListener() {
        this.guardListener = new ActionListener(this.shopManager, this.itemManager);
        return true;
    }

    @Override
    protected boolean registerEvents(PluginManager pm) {
        pm.registerEvents(this.guardListener, this);
        return true;
    }
}
