package io.izzel.taboolib.module.compat;

import me.clip.placeholderapi.PlaceholderAPI;
import io.izzel.taboolib.module.inject.TFunction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@TFunction(enable = "init")
public abstract class PlaceholderHook {

    private static PlaceholderHook impl;

    static void init() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            impl = new PlaceholderImpl();
        } else {
            impl = new AbstractImpl();
        }
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
