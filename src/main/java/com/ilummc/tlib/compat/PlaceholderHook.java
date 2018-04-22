package com.ilummc.tlib.compat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public abstract class PlaceholderHook {

    private static PlaceholderHook impl;

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            impl = new PlaceholderImpl();
        else impl = new AbstractImpl();
    }

    public static String replace(CommandSender sender, String text) {
        return sender instanceof Player ? impl.replace(((Player) sender), text) : text;
    }

    abstract String replace(Player player, String text);

    private static class PlaceholderImpl extends PlaceholderHook {

        @Override
        String replace(Player player, String text) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
    }

    private static class AbstractImpl extends PlaceholderHook {

        @Override
        String replace(Player player, String text) {
            return text;
        }
    }

}
