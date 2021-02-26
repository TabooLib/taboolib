package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * PlaceholderAPI 支持
 *
 * @author 坏黑
 * @since 2019-07-05 18:50
 */
public class PlaceholderHook {

    public interface Expansion {

        @NotNull Plugin plugin();

        @NotNull String identifier();

        @NotNull String onPlaceholderRequest(@NotNull Player p, @NotNull String params);
    }

    /**
     * 变量识别
     *
     * @param sender 执行者
     * @param text   文本
     * @return String
     */
    public static String replace(CommandSender sender, String text) {
        return sender instanceof Player ? InternalPluginBridge.handle().setPlaceholders((Player) sender, text) : text;
    }

    /**
     * 变量识别
     *
     * @param sender 执行者
     * @param text   文本列表
     * @return String
     */
    public static List<String> replace(CommandSender sender, List<String> text) {
        return sender instanceof Player ? InternalPluginBridge.handle().setPlaceholders((Player) sender, text) : text;
    }

    /**
     * @return 是否支持
     */
    public static boolean isHooked() {
        return InternalPluginBridge.handle().placeholderHooked();
    }
}
