package com.shadowdev.activityroles;

import java.util.logging.Logger;

import com.shadowdev.activityroles.commands.ActivityCommand;
import com.shadowdev.activityroles.tools.ActivityCheck;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ActivityRoles extends JavaPlugin {
    public final Logger logger = this.getLogger();
    public ActivityCheck activityCheck;
    public Boolean debug;
    public Boolean updateOnCommand;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        new Metrics(this, 14862);

        getCommand("activity").setExecutor(new ActivityCommand(this));

        this.debug = getConfig().getBoolean("debug");

        this.activityCheck = new ActivityCheck(this);
        
        this.activityCheck.logRoles();

        this.updateOnCommand = getConfig().getBoolean("update-on-command");

        if(!this.updateOnCommand) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> this.activityCheck.checkAllPlayers(), (20 * 60), (20 * 60));
        }

    }

    @Override
    public void onDisable() {
        logger.info("ActivityRoles has been disabled.");
        Bukkit.getScheduler().cancelTasks(this);
    }

}