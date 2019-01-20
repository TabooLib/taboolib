package me.skymc.taboolib;

import me.skymc.taboolib.common.nms.NMSHandler;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author sky
 */
public class TabooLib {

    private static boolean spigot = false;
    private static boolean silent = false;

    static {
        try {
            // 判断是否为独立客户端运行，不是判断 Bukkit 与 Spigot
            Class.forName("org.bukkit.Bukkit");
            spigot = true;
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取主类对象，因 Main 名称容易造成混淆所以转移至此
     */
    public static Main instance() {
        return (Main) Main.getInst();
    }

    /**
     * 插件是否为 TabooLib（沙雕方法）
     */
    public static boolean isTabooLib(Plugin plugin) {
        return plugin.equals(instance()) || plugin.getName().equals("TabooLib");
    }

    /**
     * 插件是否依赖于 TabooLib（依赖或软兼容）
     */
    public static boolean isDependTabooLib(Plugin plugin) {
        return plugin.getDescription().getDepend().contains("TabooLib") || plugin.getDescription().getSoftDepend().contains("TabooLib");
    }

    /**
     * 是否为 Spigot 核心，因 TabooLib 可在 BungeeCord 上运行所以添加此方法
     */
    public static boolean isSpigot() {
        return spigot;
    }

    /**
     * 是否为静默模式
     */
    public static boolean isSilent() {
        return silent;
    }

    /**
     * 设置静默模式，启用后将关闭部分提示
     */
    public static void setSilent(boolean silent) {
        TabooLib.silent = silent;
    }

    /**
     * 获取 TabooLib 插件版本
     */
    public static double getPluginVersion() {
        return NumberUtils.getDouble(Main.getInst().getDescription().getVersion());
    }

    /**
     * 获取服务端版本
     */
    public static String getVersion() {
        return Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    /**
     * 获取服务端版本数字
     */
    public static int getVersionNumber() {
        return getVerint();
    }

    /**
     * 重置服务器序列号
     */
    public static void resetServerUID() {
        DataUtils.getPluginData("TabooLibrary", null).set("serverUID", UUID.randomUUID().toString());
    }

    /**
     * 是否为调试模式
     */
    public static boolean isDebug() {
        return DataUtils.getPluginData("TabooLibrary", instance()).getBoolean("debug");
    }

    /**
     * 切换调试模式
     */
    public static void setDebug(boolean debug) {
        DataUtils.getPluginData("TabooLibrary", instance()).set("debug", debug);
    }

    /**
     * 发送调试信息
     */
    public static void debug(String... args) {
        debug(instance(), args);
    }

    /**
     * 发送调试信息
     */
    public static void debug(Plugin plugin, String... args) {
        if (TabooLib.isDebug()) {
            Arrays.stream(args).forEach(var -> Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[TabooLib - DEBUG][" + plugin.getName() + "] " + ChatColor.RED + var));
        }
    }

    /**
     * 获取服务器序列号
     */
    public static String getServerUID() {
        if (!DataUtils.getPluginData("TabooLibrary", null).contains("serverUID")) {
            DataUtils.getPluginData("TabooLibrary", null).set("serverUID", UUID.randomUUID().toString());
        }
        return DataUtils.getPluginData("TabooLibrary", null).getString("serverUID");
    }

    /**
     * 获取服务器 TPS
     */
    public static double[] getTPS() {
        return NMSHandler.getHandler().getTPS();
    }

    @Deprecated
    public static int getVerint() {
        String version = getVersion();
        if (version.startsWith("v1_7")) {
            return 10700;
        } else if (version.startsWith("v1_8")) {
            return 10800;
        } else if (version.startsWith("v1_9")) {
            return 10900;
        } else if (version.startsWith("v1_10")) {
            return 11000;
        } else if (version.startsWith("v1_11")) {
            return 11100;
        } else if (version.startsWith("v1_12")) {
            return 11200;
        } else if (version.startsWith("v1_13")) {
            return 11300;
        }
        return 0;
    }
}
