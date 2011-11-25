package com.bukkit.gemo.BuyCraft;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BCChatUtils {
    public static void printLine(Player player, ChatColor color, String message) {
        player.sendMessage(color + message);
    }

    public static void printInfo(Player player, ChatColor color, String message) {
        player.sendMessage(ChatColor.AQUA + "[BuyCraft] " + color + message);
    }

    public static void printError(Player player, String message) {
        printInfo(player, ChatColor.RED, message);
    }

    public static void printSuccess(Player player, String message) {
        printInfo(player, ChatColor.GREEN, message);
    }

    // WRONG SYNTAX
    public static void printWrongSyntax(Player player, String Syntax, String[] Examples) {
        BCChatUtils.printError(player, "Wrong Syntax! Use: " + Syntax);

        if (Examples.length == 1)
            BCChatUtils.printInfo(player, ChatColor.GRAY, "Example:");
        else if (Examples.length > 1)
            BCChatUtils.printInfo(player, ChatColor.DARK_RED, "Examples:");

        for (int i = 0; i < Examples.length; i++) {
            BCChatUtils.printInfo(player, ChatColor.GRAY, Examples[i]);
        }
        return;
    }
}
