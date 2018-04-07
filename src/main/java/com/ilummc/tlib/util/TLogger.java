package com.ilummc.tlib.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TLogger {

    public static final int VERBOSE = 0, FINEST = 1, FINE = 2, INFO = 3, WARN = 4, ERROR = 5, FATAL = 6;

    @Getter
    private final String pattern;

    @Getter
    private Plugin plugin;

    @Getter
    @Setter
    private int level;

    public TLogger(String pattern, Plugin plugin, int level) {
        this.pattern = pattern;
        this.plugin = plugin;
        this.level = level;
    }

    public void verbose(String msg) {
        if (level <= VERBOSE)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§f全部", msg));
    }

    public void finest(String msg) {
        if (level <= FINEST)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§e良好", msg));
    }

    public void fine(String msg) {
        if (level <= FINE)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§a正常", msg));
    }

    public void info(String msg) {
        if (level <= INFO)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§b信息", msg));
    }

    public void warn(String msg) {
        if (level <= WARN)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§6警告", "§6" + msg));
    }

    public void error(String msg) {
        if (level <= ERROR)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§c错误", "§c" + msg));
    }

    public void fatal(String msg) {
        if (level <= FATAL)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§4致命错误", "§4" + msg));
    }

}
