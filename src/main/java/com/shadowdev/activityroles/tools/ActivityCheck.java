package com.shadowdev.activityroles.tools;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.shadowdev.activityroles.ActivityRoles;

import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import org.jetbrains.annotations.Nullable;

public class ActivityCheck {

    private final ActivityRoles plugin;

    final Pattern pattern = Pattern.compile("\\d+([wdhm])", Pattern.CASE_INSENSITIVE);

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

        this.plugin.debug("All roles: " + roleNames.toString());

        roleNames.forEach(name -> {
            String id = roles.getString(name + ".id");
            String duration = roles.getString(name + ".duration");
            String type = roles.getString(name + ".type");

            Boolean isValid = verifyTimeDuration(duration);
            if (!isValid)
                this.plugin.logger.warning("Invalid duration for role " + name + ": " + duration);

            this.plugin.debug(name + " - ID:" + id + " - Duration: " + duration + " - Type: " + type);
        });

    }

    public Boolean meetsSeenRequirement(OfflinePlayer player, String requirement) {
        String timeType = requirement.substring(requirement.length() - 1);

        if (!player.hasPlayedBefore()) {

            this.plugin.debug("Player " + player.getName() + " has never played before, skipping activity check.");
            return false;
        }

        long lastSeen = player.getLastSeen();
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastSeen;
        long minutesSinceSeen = timeDifference / (1000 * 60);
        long hoursSinceSeen = minutesSinceSeen / 60;
        long daysSinceSeen = hoursSinceSeen / 24;
        long weeksSinceSeen = daysSinceSeen / 7;

        switch (timeType) {
            case "w":
                if (weeksSinceSeen <= Integer.parseInt(requirement.substring(0, requirement.length() - 1)))
                    return true;
                break;
            case "d":
                if (daysSinceSeen <= Integer.parseInt(requirement.substring(0, requirement.length() - 1)))
                    return true;
                break;
            case "h":
                if (hoursSinceSeen <= Integer.parseInt(requirement.substring(0, requirement.length() - 1)))
                    return true;
                break;
            case "m":
                if (minutesSinceSeen <= Integer.parseInt(requirement.substring(0, requirement.length() - 1)))
                    return true;
                break;
            default:
                this.plugin.logger.warning("Invalid time type: " + timeType);
                break;
        }

        return false;

    }

    public Boolean meetsTotalRequirement(Player player, String requirement) {
        String timeType = requirement.substring(requirement.length() - 1);

        if (!player.hasPlayedBefore()) {

            this.plugin.debug("Player " + player.getName() + " has never played before, skipping activity check.");

            return false;
        }

        int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE); // Docs say name misleading, actually returns
                                                                          // ticks played
        int minutesPlayed = ticksPlayed / (20 * 60);
        int hoursPlayed = minutesPlayed / (60);
        int daysPlayed = hoursPlayed / 24;
        int weeksPlayed = daysPlayed / 7;

        this.plugin.debug("Player " + player.getName() + " has played " + ticksPlayed + " ticks.");

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
            case "w":
                if (weeksPlayed >= Integer.parseInt(requirement.substring(0, requirement.length() - 1))) {
                    return true;
                }
                break;
            default:
                this.plugin.logger.warning("Invalid time type: " + timeType + " for requirement: " + requirement);
                break;
        }

        return false;
    }

    public void checkPlayer(OfflinePlayer player) {
        ConfigurationSection roles = this.plugin.getConfig().getConfigurationSection("roles");
        Set<String> roleNames = roles.getKeys(false);

        roleNames.forEach(name -> {
            Boolean doesMeet = false;
            switch (roles.getString(name + ".type")) {
                case "total":
                    @Nullable Player onlinePlayer = player.getPlayer();
                    if (onlinePlayer == null) break;
                    doesMeet = this.plugin.activityCheck.meetsTotalRequirement(onlinePlayer,
                            roles.getString(name + ".duration"));
                    break;

                case "seen":
                    doesMeet = this.plugin.activityCheck.meetsSeenRequirement(player,
                            roles.getString(name + ".duration"));
                    break;

                default:
                    this.plugin.logger
                            .warning("Invalid type for role " + name + ": " + roles.getString(name + ".type"));
                    break;
            }

            if (doesMeet) {
                giveRole(player, roles.getString(name + ".id"));
            } else {
                removeRole(player, roles.getString(name + ".id"));
            }
        });
    }

    public void checkAllPlayers() {
        this.plugin.debug("Scheduled activity check has now started for all players.");
        for (final OfflinePlayer player : this.plugin.getServer().getOfflinePlayers()) {
            this.checkPlayer(player);
        }
    }

    public void giveRole(OfflinePlayer player, String roleId) {
        AccountLinkManager accountLinkManager = DiscordSRV.getPlugin().getAccountLinkManager();
        Guild mainGuild = DiscordSRV.getPlugin().getMainGuild();

        String discordPlayerId = accountLinkManager.getDiscordId(player.getUniqueId());
        if (discordPlayerId == null || discordPlayerId.isEmpty()) {
            this.plugin.debug("Player " + player.getName() + " has no linked discord account.");
            return;
        }

        JDA jda = DiscordSRV.getPlugin().getJda();
        Role role = jda.getRoleById(roleId);
        if (role.getId().isEmpty()) {
            this.plugin.logger.warning("Role " + roleId + " does not exist.");
            return;
        }
        mainGuild.addRoleToMember(discordPlayerId, jda.getRoleById(roleId)).queue();

        this.plugin.debug("Player " + player.getName() + " has been given role " + role.getName() + ".");
    }

    public void removeRole(OfflinePlayer player, String roleId) {
        AccountLinkManager accountLinkManager = DiscordSRV.getPlugin().getAccountLinkManager();
        Guild mainGuild = DiscordSRV.getPlugin().getMainGuild();

        String discordPlayerId = accountLinkManager.getDiscordId(player.getUniqueId());
        if (discordPlayerId == null || discordPlayerId.isEmpty()) {
            this.plugin.debug("Player " + player.getName() + " has no linked discord account.");
            return;
        }

        JDA jda = DiscordSRV.getPlugin().getJda();
        Role role = jda.getRoleById(roleId);
        if (role.getId().isEmpty()) {
            this.plugin.logger.warning("Role " + roleId + " does not exist.");
            return;
        }
        mainGuild.removeRoleFromMember(discordPlayerId, jda.getRoleById(roleId)).queue();

        this.plugin.debug("Player " + player.getName() + " has been removed from role " + role.getName() + ".");
    }
}
