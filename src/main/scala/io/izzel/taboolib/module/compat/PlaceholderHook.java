package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import io.izzel.taboolib.module.inject.TFunction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@TFunction(enable = "init")
public abstract class PlaceholderHook {

    private static boolean hooked;

    static void init() {
        hooked = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static String replace(CommandSender sender, String text) {
        return sender instanceof Player && hooked ? InternalPluginBridge.handle().setPlaceholders((Player) sender, text) : text;
    }

    public static boolean isHooked() {
        return hooked;
    }
}
