package com.shadowdev.activityroles.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.shadowdev.activityroles.ActivityRoles;

public class ActivityCommand implements CommandExecutor {

    private final ActivityRoles plugin;

    public ActivityCommand(ActivityRoles activityRoles) {
        this.plugin = activityRoles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            this.plugin.activityCheck.checkPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Your roles have been synced!");
        } else
            this.plugin.logger.info("This command can only be run by players");

        return true;
    }
}