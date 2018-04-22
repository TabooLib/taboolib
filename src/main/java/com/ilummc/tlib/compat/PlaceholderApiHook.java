package com.ilummc.tlib.compat;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlaceholderApiHook {

    private static PlaceholderApiHook impl;

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            impl = new PlaceholderImpl();
        else impl = new AbstractImpl();
    }

    public static String replace(CommandSender sender, String text) {
        return sender instanceof Player ? impl.replace(((Player) sender), text) : text;
    }

    abstract String replace(Player player, String text);

    private static class PlaceholderImpl extends PlaceholderApiHook {

        @Override
        String replace(Player player, String text) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
    }

    private static class AbstractImpl extends PlaceholderApiHook {

        @Override
        String replace(Player player, String text) {
            return text;
        }
    }

}
