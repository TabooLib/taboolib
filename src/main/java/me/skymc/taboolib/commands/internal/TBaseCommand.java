package me.skymc.taboolib.commands.internal;

import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.listener.TListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * @Author sky
 * @Since 2018-05-23 2:43
 */
@TListener(register = "onLoad")
public class TBaseCommand implements Listener {

    void onLoad() {
        registerCommands();
    }

    /**
     * 向服务端注册 BaseMainCommand 类
     *
     * @param command         命令全称（需在 plugin.yml 内注册）
     * @param baseMainCommand 命令对象
     * @return {@link BaseMainCommand}
     */
    public static BaseMainCommand registerCommand(String command, BaseMainCommand baseMainCommand) {
        return BaseMainCommand.createCommandExecutor(command, baseMainCommand);
    }

    /**
     * 注册所有插件的所有 TCommand 命令
     */
    public static void registerCommands() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            try {
                registerCommand(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 注册插件的所有 TCommand 命令
     *
     * @param plugin 插件
     */
    public static void registerCommand(Plugin plugin) {
        for (Class pluginClass : FileUtils.getClasses(plugin)) {
            if (BaseMainCommand.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(TCommand.class)) {
                TCommand tCommand = (TCommand) pluginClass.getAnnotation(TCommand.class);
                try {
                    registerCommand(tCommand.name(), (BaseMainCommand) pluginClass.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        try {
            registerCommand(e.getPlugin());
        } catch (Exception ignored) {
        }
    }
}
