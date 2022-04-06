package com.shadowdev.activityroles.tools;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.shadowdev.activityroles.ActivityRoles;

import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;

public class ActivityCheck {

    private final ActivityRoles plugin;

    final Pattern pattern = Pattern.compile("\\d+([wdm])", Pattern.CASE_INSENSITIVE);

    public ActivityCheck(ActivityRoles activityRoles) {
        this.plugin = activityRoles;
    }

    public boolean verifyTimeDuration(String timeInConfig) {
        Matcher matcher = pattern.matcher(timeInConfig);
        return matcher.matches();
    }

    public void logRoles() {

        ConfigurationSection roles = this.plugin.getConfig().getConfigurationSection("roles");
        Set<String> roleNames = roles.getKeys(false);

        if (this.plugin.debug)
            this.plugin.logger.info("All roles: " + roleNames.toString());

        roleNames.forEach(name -> {
            String id = roles.getString(name + ".id");
            String duration = roles.getString(name + ".duration");

            Boolean isValid = verifyTimeDuration(duration);
            if (!isValid)
                this.plugin.logger.warning("Invalid duration for role " + name + ": " + duration);

            if (this.plugin.debug)
                this.plugin.logger.info(name + " - ID:" + id + " - Duration: " + duration);
        });

    }

    public Boolean meetsRequirement(Player player, String requirement) {
        String timeType = requirement.substring(requirement.length() - 1);

        if (!player.hasPlayedBefore() || player.getLastPlayed() == 0) {
            if (this.plugin.debug) {
                this.plugin.logger
                        .warning("Player " + player.getName() + " has never played before, skipping activity check.");
            }
            return false;
        }

        int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int minutesPlayed = ticksPlayed / (20 * 60);
        int hoursPlayed = minutesPlayed / (60);
        int daysPlayed = hoursPlayed / 24;

        if (this.plugin.debug) {
            this.plugin.logger.info("Player " + player.getName() + " has played " + ticksPlayed + " ticks.");
        }

        switch (timeType) {
            case "m":
                if (minutesPlayed >= Integer.parseInt(requirement.substring(0, requirement.length() - 1))) {
                    return true;
                }
                break;
            case "h":
                if (hoursPlayed >= Integer.parseInt(requirement.substring(0, requirement.length() - 1))) {
                    return true;
                }
                break;
            case "d":
                if (daysPlayed >= Integer.parseInt(requirement.substring(0, requirement.length() - 1))) {
                    return true;
                }
                break;
            default:
                this.plugin.logger.warning("Invalid time type: " + timeType + " for requirement: " + requirement);
                break;
        }

        return false;
    }

    public void checkAllPlayers() {
        if (this.plugin.debug) {
            this.plugin.logger.info("Scheduled activity check has now started for all online players.");
        }
        this.plugin.getServer().getOnlinePlayers().forEach(player -> {
            ConfigurationSection roles = this.plugin.getConfig().getConfigurationSection("roles");
            Set<String> roleNames = roles.getKeys(false);

            roleNames.forEach(name -> {
                Boolean doesMeet = this.plugin.activityCheck.meetsRequirement(player,
                        roles.getString(name + ".duration"));
                if (doesMeet) {
                    giveRole(player, roles.getString(name + ".id"));
                }
            });
        });
    }

    public void giveRole(Player player, String roleId) {
        AccountLinkManager accountLinkManager = DiscordSRV.getPlugin().getAccountLinkManager();
        Guild mainGuild = DiscordSRV.getPlugin().getMainGuild();

        String discordPlayerId = accountLinkManager.getDiscordId(player.getUniqueId());
        if (discordPlayerId == null || discordPlayerId.isEmpty()) {
            this.plugin.logger.warning("Player " + player.getName() + " has no linked discord account.");
            return;
        }

        JDA jda = DiscordSRV.getPlugin().getJda();
        Role role = jda.getRoleById(roleId);
        if (role.getId().isEmpty()) {
            this.plugin.logger.warning("Role " + roleId + " does not exist.");
            return;
        }
        mainGuild.addRoleToMember(discordPlayerId, jda.getRoleById(roleId)).queue();

        if (this.plugin.debug) {
            this.plugin.logger.info("Player " + player.getName() + " has been given role " + role.getName() + ".");
        }
    }

    public void removeRole(Player player, String roleId) {
        AccountLinkManager accountLinkManager = DiscordSRV.getPlugin().getAccountLinkManager();
        Guild mainGuild = DiscordSRV.getPlugin().getMainGuild();

        String discordPlayerId = accountLinkManager.getDiscordId(player.getUniqueId());
        if (discordPlayerId.isEmpty()) {
            this.plugin.logger.warning("Player " + player.getName() + " has no linked discord account.");
            return;
        }

        JDA jda = DiscordSRV.getPlugin().getJda();
        Role role = jda.getRoleById(roleId);
        if (role.getId().isEmpty()) {
            this.plugin.logger.warning("Role " + roleId + " does not exist.");
            return;
        }
        mainGuild.removeRoleFromMember(discordPlayerId, jda.getRoleById(roleId)).queue();

        if (this.plugin.debug) {
            this.plugin.logger.info("Player " + player.getName() + " has been removed from role " + role.getName() + ".");
        }
    }
}
