package com.ilummc.tlib.resources;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.inject.TLoggerManager;
import com.ilummc.tlib.util.Ref;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class TLocale {

    private TLocale() {
        throw new AssertionError();
    }

    private static JavaPlugin getCallerPlugin(Class<?> callerClass) {
        try {
            Field pluginField = callerClass.getClassLoader().getClass().getDeclaredField("plugin");
            pluginField.setAccessible(true);
            return (JavaPlugin) pluginField.get(callerClass.getClassLoader());
        } catch (Exception ignored) {
            TLib.getTLib().getLogger().error("无效的语言文件发送形式: &4" + callerClass.getName());
        }
        return (JavaPlugin) Main.getInst();
    }

    private static void sendTo(String path, CommandSender sender, String[] args, Class<?> callerClass) {
        TLocaleLoader.sendTo(getCallerPlugin(callerClass), path, sender, args);
    }

    static String asString(String path, Class<?> callerClass, String... args) {
        return TLocaleLoader.asString(getCallerPlugin(callerClass), path, args);
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
            return asString(path, Ref.getCallerClass(3).get(), args);
        } catch (Exception e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("FETCH-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("LOCALE-ERROR-REASON"), e.getMessage()));
            return "§4<" + path + "§4>";
        }
    }

    public static void reload() {
        Ref.getCallerClass(3).ifPresent(clazz -> TLocaleLoader.load(getCallerPlugin(clazz), false));
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
