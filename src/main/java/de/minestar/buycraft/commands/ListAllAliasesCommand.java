package de.minestar.buycraft.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.minestar.buycraft.core.Core;
import de.minestar.buycraft.core.Messages;
import de.minestar.buycraft.manager.ShopManager;
import de.minestar.buycraft.units.PersistentAlias;
import de.minestar.minestarlibrary.commands.AbstractCommand;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class ListAllAliasesCommand extends AbstractCommand {

    private ShopManager shopManager;

    public ListAllAliasesCommand(String syntax, String arguments, String node, ShopManager shopManager) {
        super(Core.NAME, syntax, arguments, node);
        this.description = "List all aliases";
        this.shopManager = shopManager;
    }

    public void execute(String[] args, Player player) {
        ArrayList<PersistentAlias> aliases = this.shopManager.getAllAliases();
        if (aliases == null || aliases.size() < 1) {
            PlayerUtils.sendInfo(player, Core.NAME, Messages.NO_ALIASES_SET);
            return;
        }

        // list aliases
        PlayerUtils.sendMessage(player, ChatColor.LIGHT_PURPLE, Core.NAME, Messages.LIST_OF_ALIASES);
        for (PersistentAlias alias : aliases) {
            PlayerUtils.sendInfo(player, Core.NAME, alias.getPlayerName() + " -> " + alias.getAliasName());
        }
    }
}