package me.skymc.taboolib.commands.internal;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.listener.TListener;
import me.skymc.taboolib.methods.ReflectionUtils;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author sky
 * @Since 2018-05-23 2:43
 */
@TListener
public class TCommandHandler implements Listener {

    private static SimpleCommandMap commandMap;

    public TCommandHandler() {
        try {
            Field commandMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMap.setAccessible(true);
            TCommandHandler.commandMap = (SimpleCommandMap) commandMap.get(Bukkit.getPluginManager());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            registerCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        try {
            registerCommand(e.getPlugin());
        } catch (Exception ignored) {
        }
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, CommandExecutor commandExecutor) {
        return registerPluginCommand(plugin, command, "", "/" + command, new ArrayList<>(), null, null, commandExecutor, null);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, CommandExecutor commandExecutor, TabExecutor tabExecutor) {
        return registerPluginCommand(plugin, command, "", "/" + command, new ArrayList<>(), null, null, commandExecutor, tabExecutor);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, String description, CommandExecutor commandExecutor, TabExecutor tabExecutor) {
        return registerPluginCommand(plugin, command, description, "/" + command, new ArrayList<>(), null, null, commandExecutor, tabExecutor);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, String description, String usage, CommandExecutor commandExecutor, TabExecutor tabExecutor) {
        return registerPluginCommand(plugin, command, description, usage, new ArrayList<>(), null, null, commandExecutor, tabExecutor);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, String description, String usage, List<String> aliases, CommandExecutor commandExecutor, TabExecutor tabExecutor) {
        return registerPluginCommand(plugin, command, description, usage, aliases, null, null, commandExecutor, tabExecutor);
    }

    /**
     * 向服务端动态注册命令
     *
     * @param plugin            所属插件
     * @param command           命令名称
     * @param description       命令描述
     * @param usage             命令用法
     * @param aliases           别名
     * @param permission        权限
     * @param permissionMessage 权限提示
     * @param commandExecutor   命令执行器
     * @param tabExecutor       补全执行器
     * @return 注册结果(boolean)
     */
    public static boolean registerPluginCommand(Plugin plugin, String command, String description, String usage, List<String> aliases, String permission, String permissionMessage, CommandExecutor commandExecutor, TabExecutor tabExecutor) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(command, plugin);
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(tabExecutor);
            ReflectionUtils.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "description", description);
            ReflectionUtils.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "usageMessage", usage);
            ReflectionUtils.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "aliases", aliases);
            ReflectionUtils.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "activeAliases", aliases);
            ReflectionUtils.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "permission", permission);
            ReflectionUtils.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "permissionMessage", permissionMessage);
            commandMap.register(command, pluginCommand);
            TLocale.Logger.info("COMMANDS.INTERNAL.COMMAND-CREATE", plugin.getName(), command);
            return true;
        } catch (Exception e) {
            TLocale.Logger.info("COMMANDS.INTERNAL.COMMAND-CREATE-FAILED", plugin.getName(), command, e.getMessage());
            return false;
        }
    }

    /**
     * 向服务端注册 BaseMainCommand 类
     *
     * @param command         命令全称（需在 plugin.yml 内注册）
     * @param baseMainCommand 命令对象
     * @return {@link BaseMainCommand}
     */
    public static BaseMainCommand registerCommand(TCommand tCommand, String command, BaseMainCommand baseMainCommand, Plugin plugin) {
        if (Bukkit.getPluginCommand(command) == null) {
            registerPluginCommand(
                    plugin,
                    command,
                    ArrayUtils.skipEmpty(tCommand.description(), "Registered by TabooLib."),
                    ArrayUtils.skipEmpty(tCommand.usage(), "/" + command),
                    ArrayUtils.skipEmpty(ArrayUtils.asList(tCommand.aliases()), new ArrayList<>()),
                    ArrayUtils.skipEmpty(tCommand.permission()),
                    ArrayUtils.skipEmpty(tCommand.permissionMessage()),
                    baseMainCommand,
                    baseMainCommand);
        }
        return BaseMainCommand.createCommandExecutor(command, baseMainCommand);
    }

    /**
     * 注册所有插件的所有 TCommand 命令
     */
    public static void registerCommands() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.equals(TabooLib.instance()) || plugin.getDescription().getDepend().contains("TabooLib")) {
                try {
                    registerCommand(plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    registerCommand(tCommand, tCommand.name(), (BaseMainCommand) pluginClass.newInstance(), plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取插件注册的命令
     *
     * @param command 命令名称
     * @return {@link Command}
     */
    public static Command getPluginCommand(String command) {
        return commandMap.getCommand(command);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static SimpleCommandMap getCommandMap() {
        return commandMap;
    }
}
