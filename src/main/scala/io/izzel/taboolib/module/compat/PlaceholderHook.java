package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderHook {

    public static String replace(CommandSender sender, String text) {
        return sender instanceof Player ? InternalPluginBridge.handle().setPlaceholders((Player) sender, text) : text;
    }

    public static List<String> replace(CommandSender sender, List<String> text) {
        return sender instanceof Player ? InternalPluginBridge.handle().setPlaceholders((Player) sender, text) : text;
    }

    public static boolean isHooked() {
        return InternalPluginBridge.handle().placeholderHooked();
    }
}
