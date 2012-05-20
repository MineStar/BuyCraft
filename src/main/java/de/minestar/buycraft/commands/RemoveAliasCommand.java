package de.minestar.buycraft.commands;

import org.bukkit.entity.Player;

import de.minestar.buycraft.core.Core;
import de.minestar.buycraft.core.Messages;
import de.minestar.buycraft.manager.ShopManager;
import de.minestar.buycraft.units.PersistentAlias;
import de.minestar.minestarlibrary.commands.AbstractCommand;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class RemoveAliasCommand extends AbstractCommand {

    private ShopManager shopManager;

    public RemoveAliasCommand(String syntax, String arguments, String node, ShopManager shopManager) {
        super(Core.NAME, syntax, arguments, node);
        this.description = "Remove an alias";
        this.shopManager = shopManager;
    }

    public void execute(String[] args, Player player) {
        String playerName = args[0];

        // check: player has alias?
        PersistentAlias alias = this.shopManager.getPersistentAlias(playerName);
        if (alias == null) {
            PlayerUtils.sendError(player, Core.NAME, Messages.ALIAS_PLAYER_NOT_EXISTS);
            return;
        }

        // delete alias
        if (this.shopManager.removeAlias(alias)) {
            PlayerUtils.sendSuccess(player, Core.NAME, Messages.ALIAS_DELETED);
        } else {
            PlayerUtils.sendError(player, Core.NAME, Messages.ALIAS_DELETED_ERROR);
        }
    }
}