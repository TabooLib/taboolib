package io.izzel.taboolib.module.locale.logger;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.Strings;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TLogger {

    public static final int VERBOSE = 0, FINEST = 1, FINE = 2, INFO = 3, WARN = 4, ERROR = 5, FATAL = 6;

    private final String pattern;
    private final String name;
    private int level;

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

    public static TLogger getGlobalLogger() {
        return io.izzel.taboolib.TabooLib.getLogger();
    }

    public static TLogger getUnformatted(Plugin plugin) {
        return new TLogger("§8[§3§l{0}§8][§r{1}§8] §f{2}", plugin, TLogger.FINE);
    }

    public static TLogger getUnformatted(String name) {
        return new TLogger("§8[§3§l{0}§8][§r{1}§8] §f{2}", name, TLogger.FINE);
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

    public void verbose(String msg) {
        if (level <= VERBOSE) {
            if (TabooLibAPI.isBukkit()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§fVERBOSE", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§fVERBOSE", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void finest(String msg) {
        if (level <= FINEST) {
            if (TabooLibAPI.isBukkit()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§eFINEST", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§eFINEST", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void fine(String msg) {
        if (level <= FINE) {
            if (TabooLibAPI.isBukkit()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§aFINE", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§aFINE", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void info(String msg) {
        if (level <= INFO) {
            if (TabooLibAPI.isBukkit()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§bINFO", TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§bINFO", TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void warn(String msg) {
        if (level <= WARN) {
            if (TabooLibAPI.isBukkit()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§6WARN", "§6" + TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§6WARN", "§6" + TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void error(String msg) {
        if (level <= ERROR) {
            if (TabooLibAPI.isBukkit()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§cERROR", "§c" + TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§cERROR", "§c" + TLocale.Translate.setColored(msg))));
            }
        }
    }

    public void fatal(String msg) {
        if (level <= FATAL) {
            if (TabooLibAPI.isBukkit()) {
                Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(pattern, name, "§4FATAL", "§4" + TLocale.Translate.setColored(msg)));
            } else {
                BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Strings.replaceWithOrder(pattern, name, "§4FATAL", "§4" + TLocale.Translate.setColored(msg))));
            }
        }
    }
}
