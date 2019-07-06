package io.izzel.taboolib;

import io.izzel.taboolib.module.nms.NMSHandler;
import io.izzel.taboolib.module.db.yaml.PluginDataManager;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

/**
 * @Author 坏黑
 * @Since 2019-07-05 14:31
 */
public class TabooLibAPI {

    private static boolean bukkit;
    private static boolean originLoaded;

    static {
        try {
            // 判断是否基于 Bukkit 运行
            bukkit = Class.forName("org.bukkit.Bukkit") != null;
            // 获取 TabooLib4.x 版本是否载入
            originLoaded = Bukkit.getPluginManager().getPlugin("TabooLib") != null;
        } catch (Exception ignored) {
        }
    }

    public static boolean isBukkit() {
        return bukkit;
    }

    public static boolean isOriginLoaded() {
        return originLoaded;
    }

    public static boolean isDependTabooLib(Plugin plugin) {
        return PluginLoader.isPlugin(plugin);
    }

    public static double[] getTPS() {
        return NMSHandler.getHandler().getTPS();
    }

    public static boolean isDebug() {
        return PluginDataManager.getPluginData("TabooLibrary", TabooLib.getPlugin()).getBoolean("debug");
    }

    public static void setDebug(boolean debug) {
        PluginDataManager.getPluginData("TabooLibrary", TabooLib.getPlugin()).set("debug", debug);
    }

    public static void debug(String... args) {
        debug(TabooLib.getPlugin(), args);
    }

    public static void debug(Plugin plugin, String... args) {
        if (!isDebug()) {
            return;
        }
        for (String line : args) {
            Bukkit.getConsoleSender().sendMessage("§4[" + plugin.getName() + "][DEBUG] §c" + line);
        }
    }

    public static boolean dispatchCommand(CommandSender sender, String command) {
        try {
            if ((sender instanceof Player)) {
                PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent((Player) sender, "/" + command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled() || Strings.isBlank(e.getMessage()) || !e.getMessage().startsWith("/")) {
                    return false;
                }
                return Bukkit.dispatchCommand(e.getPlayer(), e.getMessage().substring(1));
            } else {
                ServerCommandEvent e = new ServerCommandEvent(sender, command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled() || Strings.isBlank(e.getCommand())) {
                    return false;
                }
                return Bukkit.dispatchCommand(e.getSender(), e.getCommand());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
