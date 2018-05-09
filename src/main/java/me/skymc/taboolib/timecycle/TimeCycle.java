package me.skymc.taboolib.timecycle;

import me.skymc.taboolib.other.DateUtils;
import org.bukkit.plugin.Plugin;

public class TimeCycle {

    private String name;
    private Plugin plugin;

    private long cycle;

    public TimeCycle(String name, long cycle, Plugin plugin) {
        this.name = name;
        this.cycle = cycle;
        this.plugin = plugin;

        long millisHour = DateUtils.getTime(DateUtils.HOUR_OF_DAY) * 60L * 60L * 1000L;
        long millisMinute = DateUtils.getTime(DateUtils.MINUTE) * 60L * 1000L;

        long time = System.currentTimeMillis() - millisHour - millisMinute;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public long getCycle() {
        return cycle;
    }
}
