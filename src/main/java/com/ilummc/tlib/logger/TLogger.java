package com.ilummc.tlib.logger;

import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TLogger {

    public static final int VERBOSE = 0, FINEST = 1, FINE = 2, INFO = 3, WARN = 4, ERROR = 5, FATAL = 6;

    private static TLogger globalLogger = new TLogger("§8[§3§lTabooLib§8][§r{1}§8] §f{2}", Main.getInst(), TLogger.FINE);
    private final String pattern;
    private String name;
    private int level;

    public static TLogger getGlobalLogger() {
        return globalLogger;
    }

    public String getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public TLogger(String pattern, Plugin plugin, int level) {
        this.pattern = pattern;
        this.name = plugin.getName();
        this.level = level;
    }

    public TLogger(String pattern, String name, int level) {
        this.pattern = pattern;
        this.name = name;
        this.level = level;
    }

    public void verbose(String msg) {
        if (level <= VERBOSE) {
            if (TabooLib.isSpigot()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§f全部", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§f全部", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void finest(String msg) {
        if (level <= FINEST) {
            if (TabooLib.isSpigot()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§e良好", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§e良好", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void fine(String msg) {
        if (level <= FINE) {
            if (TabooLib.isSpigot()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§a正常", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§a正常", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void info(String msg) {
        if (level <= INFO) {
            if (TabooLib.isSpigot()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§b信息", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§b信息", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void warn(String msg) {
        if (level <= WARN) {
            if (TabooLib.isSpigot()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§6警告", "§6" + TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§6警告", "§6" + TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void error(String msg) {
        if (level <= ERROR) {
            if (TabooLib.isSpigot()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§c错误", "§c" + TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§c错误", "§c" + TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void fatal(String msg) {
        if (level <= FATAL) {
            if (TabooLib.isSpigot()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§4致命错误", "§4" + TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§4致命错误", "§4" + TLocale.Translate.setColored(msg))));
            }
        }
    }

    public static TLogger getUnformatted(Plugin plugin) {
        return new TLogger("§8[§3§l{0}§8][§r{1}§8] §f{2}", plugin, TLogger.FINE);
    }

    public static TLogger getUnformatted(String name) {
        return new TLogger("§8[§3§l{0}§8][§r{1}§8] §f{2}", name, TLogger.FINE);
    }
}
