package de.minestar.buycraft.commands;

import org.bukkit.entity.Player;

import de.minestar.buycraft.core.Core;
import de.minestar.buycraft.core.Messages;
import de.minestar.buycraft.manager.ShopManager;
import de.minestar.buycraft.units.PersistentAlias;
import de.minestar.minestarlibrary.commands.AbstractCommand;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class AddAliasCommand extends AbstractCommand {

    private ShopManager shopManager;

    public AddAliasCommand(String syntax, String arguments, String node, ShopManager shopManager) {
        super(Core.NAME, syntax, arguments, node);
        this.description = "Add an alias";
        this.shopManager = shopManager;
    }

    public void execute(String[] args, Player player) {

        String playerName = args[0];
        String aliasName = args[1];
        if (aliasName.length() > 13) {
            PlayerUtils.sendError(player, pluginName, "The aliasname is too long (max. 13 chars).");
            return;
        }

        // check: player has no alias?
        PersistentAlias alias = this.shopManager.getPersistentAlias(playerName);
        if (alias != null) {
            PlayerUtils.sendError(player, Core.NAME, Messages.ALIAS_EXISTS_PLAYER);
            return;
        }
        // check: alias does not exist?
        alias = this.shopManager.getPersistentAlias(aliasName);
        if (alias != null) {
            PlayerUtils.sendError(player, Core.NAME, Messages.ALIAS_EXISTS_ALIAS);
            return;
        }

        // create alias
        alias = this.shopManager.addAlias(playerName, aliasName);
        if (alias != null) {
            PlayerUtils.sendSuccess(player, Core.NAME, Messages.ALIAS_CREATED);
        } else {
            PlayerUtils.sendError(player, Core.NAME, Messages.ALIAS_CREATED_ERROR);
        }
    }
}