package io.izzel.taboolib.module.compat;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * PlaceholderAPI 支持
 *
 * @Author 坏黑
 * @Since 2019-07-05 18:50
 */
public class PlaceholderHook {

    /**
     * 变量识别
     *
     * @param sender 执行者
     * @param text   文本
     */
    public static String replace(CommandSender sender, String text) {
        return sender instanceof Player ? InternalPluginBridge.handle().setPlaceholders((Player) sender, text) : text;
    }

    /**
     * 变量识别
     *
     * @param sender 执行者
     * @param text   文本列表
     */
    public static List<String> replace(CommandSender sender, List<String> text) {
        return sender instanceof Player ? InternalPluginBridge.handle().setPlaceholders((Player) sender, text) : text;
    }

    /**
     * 是否支持
     */
    public static boolean isHooked() {
        return InternalPluginBridge.handle().placeholderHooked();
    }
}
