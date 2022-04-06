package com.shadowdev.activityroles.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Set;

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

            if (args.length == 0) {
                ConfigurationSection roles = this.plugin.getConfig().getConfigurationSection("roles");
                Set<String> roleNames = roles.getKeys(false);

                roleNames.forEach(name -> {
                    Boolean doesMeet = this.plugin.activityCheck.meetsRequirement(player,
                            roles.getString(name + ".duration"));
                    if (doesMeet)
                        this.plugin.activityCheck.giveRole(player, (roles.getString(name + ".id")));
                    player.sendMessage(name + ": " + (doesMeet ? ChatColor.GREEN + "requirement met" : ChatColor.RED + "requirement not met"));
                });

                return true;
            }

        } else
            this.plugin.logger.info("This command can only be run by players");

        return true;
    }
}