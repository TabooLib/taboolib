package com.ilummc.tlib.logger;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public class TLogger {

    public static final int VERBOSE = 0, FINEST = 1, FINE = 2, INFO = 3, WARN = 4, ERROR = 5, FATAL = 6;

    private static TLogger globalLogger = new TLogger("§8[§3§lTabooLib§8][§r{1}§8] §f{2}", Main.getInst(), TLogger.FINE);
    private final String pattern;
    private Plugin plugin;
    private int level;

    public static TLogger getGlobalLogger() {
        return globalLogger;
    }

    public String getPattern() {
        return pattern;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public TLogger(String pattern, Plugin plugin, int level) {
        this.pattern = pattern;
        this.plugin = plugin;
        this.level = level;
    }

    public void verbose(String msg) {
        if (level <= VERBOSE)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§f全部", ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public void finest(String msg) {
        if (level <= FINEST)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§e良好", ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public void fine(String msg) {
        if (level <= FINE)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§a正常", ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public void info(String msg) {
        if (level <= INFO)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§b信息", ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public void warn(String msg) {
        if (level <= WARN)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§6警告", "§6" + ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public void error(String msg) {
        if (level <= ERROR)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§c错误", "§c" + ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public void fatal(String msg) {
        if (level <= FATAL)
            Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, plugin.getName(), "§4致命错误", "§4" + ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public static TLogger getUnformatted(Plugin plugin) {
        return new TLogger("§8[§3§l{0}§8][§r{1}§8] §f{2}", plugin, TLogger.FINE);
    }

}
