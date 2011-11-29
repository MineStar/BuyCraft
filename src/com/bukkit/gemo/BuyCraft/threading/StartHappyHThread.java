package com.bukkit.gemo.BuyCraft.threading;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.BuyCraft.BCBlockListener;
import com.bukkit.gemo.BuyCraft.BCCore;
import com.bukkit.gemo.BuyCraft.BCUserShop;

public class StartHappyHThread extends TimerTask {

    private int[] itemIDs;
    private Server server;

    public StartHappyHThread(Server server) {
        this.server = server;
        loadItems();
    }

    @Override
    public void run() {

        // get random item which is on market
        Random rand = new Random();
        int item = 0;
        do {
            item = itemIDs[rand.nextInt(itemIDs.length)];
        } while (!isOnMarket(item));

        ItemStack i = new ItemStack(item);
        // calculate bonus blocks given by server - 1 to 2
        double bonus = rand.nextDouble() + 1;
        String text = "Stullen Andi : Auf dem Usermarkt gibt es " + bonus + "%  Bonus auf " + i.getType().toString();
        for (Player p : server.getOnlinePlayers())
            p.sendMessage(text);

        // Start Happy Hour
        BCCore.happyHourItem = item;
    }

    private boolean isOnMarket(int itemID) {
        HashMap<String, BCUserShop> shops = BCBlockListener.userShopList;
        for (BCUserShop shop : shops.values()) {
            if (shop.hasAmountOfItem(itemID, (short) 0, 1))
                return true;
        }
        return false;
    }

    @SuppressWarnings({"unchecked"})
    private void loadItems() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load("plugins/BuyCraft/announcedItems.yml");
            List<Integer> list = config.getList("items");
            itemIDs = new int[list.size()];
            for (int i = 0; i < list.size(); ++i)
                itemIDs[i] = list.get(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
