package com.ilummc.tlib.resources;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.bungee.api.ChatColor;
import com.ilummc.tlib.inject.TLoggerManager;
import com.ilummc.tlib.util.Ref;
import com.ilummc.tlib.util.Strings;
import me.clip.placeholderapi.PlaceholderAPI;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TLocale {

    private TLocale() {
        throw new AssertionError();
    }

    static String asString(String path, Class<?> callerClass, String... args) {
        return TLocaleLoader.asString(getCallerPlugin(callerClass), path, args);
    }

    static List<String> asStringList(String path, Class<?> callerClass, String... args) {
        return TLocaleLoader.asStringList(getCallerPlugin(callerClass), path, args);
    }

    private static void sendTo(String path, CommandSender sender, String[] args, Class<?> callerClass) {
        TLocaleLoader.sendTo(getCallerPlugin(callerClass), path, sender, args);
    }

    public static void sendToConsole(String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, Bukkit.getConsoleSender(), args, clazz));
    }

    public static void sendTo(CommandSender sender, String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }

    public static void sendTo(String path, CommandSender sender, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }

    public static String asString(String path, String... args) {
        try {
            return asString(path, Ref.getCallerClassNotOptional(3), args);
        } catch (Exception e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("FETCH-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("LOCALE-ERROR-REASON"), e.getMessage()));
            return "§4<" + path + "§4>";
        }
    }

    public static List<String> asStringList(String path, String... args) {
        try {
            return asStringList(path, Ref.getCallerClassNotOptional(3), args);
        } catch (Exception e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("FETCH-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("LOCALE-ERROR-REASON"), e.getMessage()));
            return Collections.singletonList("§4<" + path + "§4>");
        }
    }

    public static void reload() {
        Ref.getCallerClass(3).ifPresent(clazz -> TLocaleLoader.load(getCallerPlugin(clazz), false));
    }

    private static JavaPlugin getCallerPlugin(Class<?> callerClass) {
        try {
            Field pluginField = callerClass.getClassLoader().getClass().getDeclaredField("plugin");
            pluginField.setAccessible(true);
            return (JavaPlugin) pluginField.get(callerClass.getClassLoader());
        } catch (Exception ignored) {
            TLocale.Logger.error("LOCALE.CALLER-PLUGIN-NOT-FOUND", callerClass.getName());
        }
        return (JavaPlugin) Main.getInst();
    }

    public static final class Translate extends TLocale {

        public static boolean isPlaceholderUseDefault() {
            return Main.getInst().getConfig().getBoolean("LOCALE.USE_PAPI", false);
        }

        public static boolean isPlaceholderPluginEnabled() {
            return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
        }

        public static String setColored(String args) {
            return ChatColor.translateAlternateColorCodes('&', args);
        }

        public static List<String> setColored(List<String> args) {
            return args.stream().map(var -> ChatColor.translateAlternateColorCodes('&', var)).collect(Collectors.toList());
        }

        public static String setPlaceholders(CommandSender sender, String args) {
            return isPlaceholderPluginEnabled() ? sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, args) : args : args;
        }

        public static List<String> setPlaceholders(CommandSender sender, List<String> args) {
            return isPlaceholderPluginEnabled() ? sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, args) : args : args;
        }
    }

    public static final class Logger extends TLocale {

        public static void info(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> TLoggerManager.getLogger(TLocale.getCallerPlugin(clazz)).info(asString(path, clazz, args)));
        }

        public static void warn(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> TLoggerManager.getLogger(TLocale.getCallerPlugin(clazz)).warn(asString(path, clazz, args)));
        }

        public static void error(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> TLoggerManager.getLogger(TLocale.getCallerPlugin(clazz)).error(asString(path, clazz, args)));
        }

        public static void fatal(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> TLoggerManager.getLogger(TLocale.getCallerPlugin(clazz)).fatal(asString(path, clazz, args)));
        }

        public static void fine(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> TLoggerManager.getLogger(TLocale.getCallerPlugin(clazz)).fine(asString(path, clazz, args)));
        }

        public static void finest(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> TLoggerManager.getLogger(TLocale.getCallerPlugin(clazz)).finest(asString(path, clazz, args)));
        }

        public static void verbose(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> TLoggerManager.getLogger(TLocale.getCallerPlugin(clazz)).verbose(asString(path, clazz, args)));
        }
    }

}
