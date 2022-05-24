package com.shadowdev.activityroles;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import com.shadowdev.activityroles.commands.ActivityCommand;
import com.shadowdev.activityroles.tools.ActivityCheck;

import org.apache.commons.io.FileUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ActivityRoles extends JavaPlugin {
    public final Logger logger = this.getLogger();
    public ActivityCheck activityCheck;

    public final int currentConfig = 2;

    @Override
    public void onEnable() {
        Boolean configIsValid = checkConfig();
        if (configIsValid) {
            getConfig().options().copyDefaults();
            saveDefaultConfig();

            new Metrics(this, 14862);

            getCommand("activity").setExecutor(new ActivityCommand(this));

            this.activityCheck = new ActivityCheck(this);

            this.activityCheck.logRoles();

            Boolean autosyncEnabled = getConfig().getBoolean("autosync-enabled");
            int autosyncInterval = getConfig().getInt("autosync-interval");

            if (autosyncEnabled) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> this.activityCheck.checkAllPlayers(),
                        (20 * autosyncInterval), (20 * autosyncInterval));
                debug("Autosync enabled. Interval: " + autosyncInterval + " seconds.");
            }
        }

    }

    @Override
    public void onDisable() {
        logger.info("ActivityRoles has been disabled.");
        Bukkit.getScheduler().cancelTasks(this);
    }

    public Boolean checkConfig() {
        int configVersion = this.getConfig().getInt("config-version");
        if (configVersion != this.currentConfig) {
            File oldConfigTo = new File(this.getDataFolder(), "config-old-" + configVersion + ".yml");
            File old = new File(this.getDataFolder(), "config.yml");
            try {
                FileUtils.moveFile(old, oldConfigTo);
                getConfig().options().copyDefaults();
                saveDefaultConfig();
                this.logger.severe("Your config is outdated. Your old config has been moved to " + oldConfigTo.getName()
                        + ", and the new version has been applied in its place.");
            } catch (Exception e) {
                File newConfig = new File(this.getDataFolder(), "config-new.yml");
                InputStream newConfigData = this.getResource("config.yml");
                try {
                    FileUtils.copyInputStreamToFile(newConfigData, newConfig);
                    this.logger.severe(
                            "Your config is outdated, but I was unable to replace your old config. Instead, the new config has been saved to "
                                    + newConfig.getName() + ".");
                } catch (Exception e1) {
                    this.logger.severe(
                            "Your config is outdated, but I could not move your old config to a backup or copy in the new config format.");
                }

            }

            this.logger.severe(
                    "The plugin will now disable, please migrate the values from your old config to the new one.");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        } else {
            File newConfig = new File(this.getDataFolder(), "config-new.yml");
            if (newConfig.exists())
                FileUtils.deleteQuietly(newConfig);
        }
        return true;
    }

    public void debug(String message) {
        if (this.getConfig().getBoolean("debug")) {
            this.logger.info(message);
        }
    }

}