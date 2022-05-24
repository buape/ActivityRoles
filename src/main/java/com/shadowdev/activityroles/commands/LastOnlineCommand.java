package com.shadowdev.activityroles.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Date;

import com.shadowdev.activityroles.ActivityRoles;

public class LastOnlineCommand implements CommandExecutor {

    private final ActivityRoles plugin;

    public LastOnlineCommand(ActivityRoles activityRoles) {
        this.plugin = activityRoles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /lastonline <player>");
            return true;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        if(target.getLastSeen() == 0) {
            sender.sendMessage(ChatColor.RED + "Player has never played before.");
            return true;
        }
        Date lastSeen = new Date(target.getLastSeen());
        sender.sendMessage(target.getName() + " was last seen " + lastSeen.toString());

        return true;
    }
}