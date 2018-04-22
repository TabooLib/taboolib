package com.ilummc.tlib.resources;

import com.ilummc.tlib.util.Ref;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public final class TLocale {

    private TLocale() {
        throw new AssertionError();
    }

    private static void sendTo(String path, CommandSender sender, String[] args, Class<?> callerClass) {
        try {
            Field pluginField = callerClass.getClassLoader().getClass().getDeclaredField("plugin");
            pluginField.setAccessible(true);
            JavaPlugin plugin = (JavaPlugin) pluginField.get(callerClass.getClassLoader());
            if (args.length == 0)
                LocaleLoader.sendTo(plugin, path, sender);
            else
                LocaleLoader.sendTo(plugin, path, sender, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTo(String path, CommandSender sender, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }

    public static void sendTo(String path, CommandSender sender) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, new String[0], clazz));
    }

    public static void sendTo(CommandSender sender, String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }

    public static void sendTo(CommandSender sender, String path) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, new String[0], clazz));
    }

    public static void sendToConsole(String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, Bukkit.getConsoleSender(), args, clazz));
    }

    public static void sendToConsole(String path) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, Bukkit.getConsoleSender(), new String[0], clazz));
    }

}
