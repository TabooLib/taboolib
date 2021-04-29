package io.izzel.taboolib;

import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.nms.NMSFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * TabooLib 中心工具
 *
 * @author 坏黑
 * @since 2019-07-05 14:31
 */
public class TabooLibAPI {

    private static boolean isBukkit;

    private static final boolean isForge = forName("net.minecraftforge.classloading.FMLForgePlugin", Plugin.class.getClassLoader()) != null || forName("net.minecraftforge.common.MinecraftForge", Plugin.class.getClassLoader()) != null;

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
            isBukkit = true;
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取插件支持库，提供与其他插件交互的相关工具。
     *
     * @return {@link InternalPluginBridge}
     */
    @NotNull
    public static InternalPluginBridge getPluginBridge() {
        return InternalPluginBridge.handle();
    }

    /**
     * @return 当前服务端是否基于 Bukkit（含 Spigot、Paper 等）
     */
    public static boolean isBukkit() {
        return isBukkit;
    }

    /**
     * @return 当前服务端是否含有 Forge
     */
    public static boolean isForge() {
        return isForge;
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
     * @return 服务端内部工具
     */
    @NotNull
    public static NMSFactory nmsFactory() {
        return NMSFactory.INSTANCE;
    }
}
