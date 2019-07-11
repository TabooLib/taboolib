package io.izzel.taboolib;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * @Author 坏黑
 * @Since 2019-07-05 14:31
 */
public class TabooLibAPI {

    private static boolean bukkit;

    static {
        try {
            // 判断是否基于 Bukkit 运行
            bukkit = Class.forName("org.bukkit.Bukkit") != null;
        } catch (Exception ignored) {
        }
    }

    public static InternalPluginBridge getPluginBridge() {
        return InternalPluginBridge.handle();
    }

    public static boolean isBukkit() {
        return bukkit;
    }

    public static boolean isOriginLoaded() {
        return Bukkit.getPluginManager().getPlugin("TabooLib") != null;
    }

    public static boolean isDependTabooLib(Plugin plugin) {
        return PluginLoader.isPlugin(plugin);
    }

    public static double[] getTPS() {
        return NMS.handle().getTPS();
    }

    public static boolean isDebug() {
        return Local.get().get("data").getBoolean("debug");
    }

    public static void debug(boolean debug) {
        Local.get().get("data").set("debug", debug);
    }

    public static void debug(String... args) {
        debug(TabooLib.getPlugin(), args);
    }

    public static void debug(Plugin plugin, String... args) {
        if (isDebug()) {
            Arrays.stream(args).forEach(line -> Bukkit.getConsoleSender().sendMessage("§4[" + plugin.getName() + "][DEBUG] §c" + line));
        }
    }
}
