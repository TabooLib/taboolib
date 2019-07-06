package io.izzel.taboolib.module.command;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.command.base.BaseCommand;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.module.lite.SimpleReflection;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-23 2:43
 */
@TFunction(enable = "init")
public class TCommandHandler {

    private static SimpleCommandMap commandMap;
    private static Map<String, Command> knownCommands;

    static void init() {
        SimpleReflection.saveField(SimplePluginManager.class, "commandMap");
        SimpleReflection.saveField(SimpleCommandMap.class, "knownCommands");
        commandMap = (SimpleCommandMap) SimpleReflection.getFieldValue(SimplePluginManager.class, Bukkit.getPluginManager(), "commandMap");
        knownCommands = (Map<String, Command>) SimpleReflection.getFieldValue(SimpleCommandMap.class, commandMap, "knownCommands");
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, CommandExecutor commandExecutor) {
        return registerPluginCommand(plugin, command, "", "/" + command, new ArrayList<>(), null, null, commandExecutor, null);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, "", "/" + command, new ArrayList<>(), null, null, commandExecutor, tabCompleter);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, String description, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, description, "/" + command, new ArrayList<>(), null, null, commandExecutor, tabCompleter);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, String description, String usage, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, description, usage, new ArrayList<>(), null, null, commandExecutor, tabCompleter);
    }

    public static boolean registerPluginCommand(Plugin plugin, String command, String description, String usage, List<String> aliases, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, description, usage, aliases, null, null, commandExecutor, tabCompleter);
    }

    /**
     * 获取插件注册的命令
     */
    public static Command getPluginCommand(String command) {
        return commandMap.getCommand(command);
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
     * @param tabCompleter      补全执行器
     * @return 注册结果(boolean)
     */
    public static boolean registerPluginCommand(Plugin plugin, String command, String description, String usage, List<String> aliases, String permission, String permissionMessage, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(command, plugin);
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(tabCompleter);
            Reflection.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "description", description);
            Reflection.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "usageMessage", usage);
            Reflection.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "aliases", aliases.stream().map(String::toLowerCase).collect(Collectors.toList()));
            Reflection.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "activeAliases", aliases.stream().map(String::toLowerCase).collect(Collectors.toList()));
            Reflection.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "permission", permission);
            Reflection.setValue(pluginCommand, pluginCommand.getClass().getSuperclass(), true, "permissionMessage", permissionMessage);
            commandMap.register(plugin.getName(), pluginCommand);
            TabooLibAPI.debug("Command " + command + " created. (" + plugin.getName() + ")");
            return true;
        } catch (Exception e) {
            TLocale.Logger.error("COMMANDS.INTERNAL.COMMAND-CREATE-FAILED", plugin.getName(), command, e.toString());
            e.printStackTrace();
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
    public static BaseMainCommand registerCommand(BaseCommand tCommand, String command, BaseMainCommand baseMainCommand, Plugin plugin) {
        if (Bukkit.getPluginCommand(command) == null) {
            registerPluginCommand(
                    plugin,
                    command,
                    ArrayUtil.skipEmpty(tCommand.description(), "Registered by TabooLib."),
                    ArrayUtil.skipEmpty(tCommand.usage(), "/" + command),
                    ArrayUtil.skipEmpty(ArrayUtil.asList(tCommand.aliases()), new ArrayList<>()),
                    ArrayUtil.skipEmpty(tCommand.permission()),
                    ArrayUtil.skipEmpty(tCommand.permissionMessage()),
                    baseMainCommand,
                    baseMainCommand);
        }
        return BaseMainCommand.createCommandExecutor(command, baseMainCommand);
    }


    /**
     * 注册插件的所有 TCommand 命令
     */
    public static void registerCommand(Plugin plugin) {
        for (Class pluginClass : Files.getClasses(plugin)) {
            if (BaseMainCommand.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(BaseCommand.class)) {
                BaseCommand tCommand = (BaseCommand) pluginClass.getAnnotation(BaseCommand.class);
                try {
                    registerCommand(tCommand, tCommand.name(), (BaseMainCommand) pluginClass.newInstance(), plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    public static Map<String, Command> getKnownCommands() {
        return knownCommands;
    }
}
