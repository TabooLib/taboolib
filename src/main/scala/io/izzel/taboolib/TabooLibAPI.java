package io.izzel.taboolib;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * TabooLib 中心工具
 *
 * @author 坏黑
 * @since 2019-07-05 14:31
 */
public class TabooLibAPI {

    private static boolean bukkit;
    private static final boolean forge = forName("net.minecraftforge.classloading.FMLForgePlugin", Plugin.class.getClassLoader()) != null || forName("net.minecraftforge.common.MinecraftForge", Plugin.class.getClassLoader()) != null;

    /**
     * 通过类加载器获取插件类
     *
     * @param name   名称
     * @param loader 类加载器
     */
    private static Class<?> forName(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (Throwable ignored) {
            return null;
        }
    }

    static {
        try {
            // 判断是否基于 Bukkit 运行
            Class.forName("org.bukkit.Bukkit");
            bukkit = true;
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取插件支持库，提供与其他插件交互的相关工具。
     *
     * @return {@link InternalPluginBridge}
     */
    public static InternalPluginBridge getPluginBridge() {
        return InternalPluginBridge.handle();
    }

    /**
     * @return 当前服务端是否基于 Bukkit（含 Spigot、Paper 等）
     */
    public static boolean isBukkit() {
        return bukkit;
    }

    /**
     * @return 当前服务端是否含有 Forge
     */
    public static boolean isForge() {
        return forge;
    }

    /**
     * @return TabooLib 4.X 插件版本是否被加载
     */
    public static boolean isOriginLoaded() {
        return Bukkit.getPluginManager().getPlugin("TabooLib") != null;
    }

    /**
     * 检测该插件是否基于 TabooLib
     *
     * @param plugin 插件实例
     * @return boolean
     */
    public static boolean isDependTabooLib(Plugin plugin) {
        return PluginLoader.isPlugin(plugin);
    }

    /**
     * @return 服务端 TPS 运行状态
     */
    public static double[] getTPS() {
        return NMS.handle().getTPS();
    }

    /**
     * @return 检测是否处于调试模式，可通过控制台指令 /tdebug 指令切换调试模式。
     */
    public static boolean isDebug() {
        return Local.get().get("data").getBoolean("debug");
    }

    /**
     * 设置调试模式
     *
     * @param debug debug
     */
    public static void debug(boolean debug) {
        Local.get().get("data").set("debug", debug);
    }

    /**
     * 输出调试信息
     *
     * @param args 调试信息，不通过颜色转换
     */
    public static void debug(String... args) {
        debug(TabooLib.getPlugin(), args);
    }

    /**
     * 已特定插件视角输出调试信息
     *
     * @param plugin 插件实例
     * @param args   调试信息，不通过颜色转换
     */
    public static void debug(Plugin plugin, String... args) {
        if (isDebug()) {
            Arrays.stream(args).forEach(line -> Bukkit.getConsoleSender().sendMessage("§4[" + plugin.getName() + "][DEBUG] §c" + line));
        }
    }
}
