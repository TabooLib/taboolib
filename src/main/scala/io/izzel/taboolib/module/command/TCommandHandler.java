package io.izzel.taboolib.module.command;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.kotlin.Reflex;
import io.izzel.taboolib.module.command.base.BaseCommand;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.module.inject.TInjectHelper;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 命令注册工具
 * 无需在 plugin.yml 文件中声明命令
 *
 * @author sky
 * @since 2018-05-23 2:43
 */
public class TCommandHandler {

    private static SimpleCommandMap commandMap;
    private static Map<String, Command> knownCommands;

    @TFunction.Init
    static void init() {
        commandMap = new Reflex(SimplePluginManager.class).instance(Bukkit.getPluginManager()).get("commandMap");
        knownCommands = new Reflex(SimpleCommandMap.class).instance(commandMap).get("knownCommands");
    }

    @Nullable
    public static PluginCommand registerPluginCommand(Plugin plugin, String command, CommandExecutor commandExecutor) {
        return registerPluginCommand(plugin, command, "", "/" + command, new ArrayList<>(), null, null, commandExecutor, null);
    }

    @Nullable
    public static PluginCommand registerPluginCommand(Plugin plugin, String command, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, "", "/" + command, new ArrayList<>(), null, null, commandExecutor, tabCompleter);
    }

    @Nullable
    public static PluginCommand registerPluginCommand(Plugin plugin, String command, String description, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, description, "/" + command, new ArrayList<>(), null, null, commandExecutor, tabCompleter);
    }

    @Nullable
    public static PluginCommand registerPluginCommand(Plugin plugin, String command, String description, String usage, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, description, usage, new ArrayList<>(), null, null, commandExecutor, tabCompleter);
    }

    @Nullable
    public static PluginCommand registerPluginCommand(Plugin plugin, String command, String description, String usage, List<String> aliases, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        return registerPluginCommand(plugin, command, description, usage, aliases, null, null, commandExecutor, tabCompleter);
    }

    /**
     * 获取插件注册的命令
     *
     * @param command 命令
     * @return {@link Command}
     */
    @Nullable
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
     * @return PluginCommand
     */
    @Nullable
    public static PluginCommand registerPluginCommand(Plugin plugin, String command, String description, String usage, List<String> aliases, String permission, String permissionMessage, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(command, plugin);
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(tabCompleter);
            Reflex reflex = new Reflex(pluginCommand.getClass().getSuperclass()).instance(pluginCommand);
            reflex.set("description", description);
            reflex.set("usageMessage", usage);
            reflex.set("aliases", aliases.stream().map(String::toLowerCase).collect(Collectors.toList()));
            reflex.set("activeAliases", aliases.stream().map(String::toLowerCase).collect(Collectors.toList()));
            reflex.set("permission", permission);
            reflex.set("permissionMessage", permissionMessage);
            commandMap.register(plugin.getName(), pluginCommand);
            return pluginCommand;
        } catch (Exception e) {
            TLocale.Logger.error("COMMANDS.INTERNAL.COMMAND-CREATE-FAILED", plugin.getName(), command, e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 向服务端注册 BaseMainCommand 类
     *
     * @param baseCommand     命令实例
     * @param command         命令全称（需在 plugin.yml 内注册）
     * @param baseMainCommand 命令对象
     * @param plugin          插件
     */
    public static void registerCommand(BaseCommand baseCommand, String command, BaseMainCommand baseMainCommand, Plugin plugin) {
        if (baseCommand.permissionDefault() == PermissionDefault.TRUE || baseCommand.permissionDefault() == PermissionDefault.NOT_OP) {
            String permission = baseCommand.permission();
            if (permission.isEmpty()) {
                permission = plugin.getName().toLowerCase() + ".command.use";
            }
            if (Bukkit.getPluginManager().getPermission(permission) != null) {
                try {
                    Permission p = new Permission(permission, baseCommand.permissionDefault());
                    Bukkit.getPluginManager().addPermission(p);
                    Bukkit.getPluginManager().recalculatePermissionDefaults(p);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        TCommandHandler.getKnownCommands().remove(command);
        BaseMainCommand.createCommandExecutor(registerPluginCommand(
                plugin,
                command,
                skip(baseCommand.description(), "/" + command),
                skip(baseCommand.usage(), "/" + command),
                Arrays.asList(baseCommand.aliases()),
                skip(baseCommand.permission()),
                skip(baseCommand.permissionMessage()),
                baseMainCommand,
                baseMainCommand), baseMainCommand);
    }

    /**
     * 注册插件的所有 TCommand 命令
     *
     * @param plugin 插件
     */
    public static void registerCommand(Plugin plugin) {
        for (Class<?> pluginClass : TabooLibLoader.getPluginClassSafely(plugin)) {
            if (BaseMainCommand.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(BaseCommand.class)) {
                BaseCommand baseCommand = pluginClass.getAnnotation(BaseCommand.class);
                try {
                    Object instance = TInjectHelper.getInstance(pluginClass, plugin).get(0);
                    registerCommand(baseCommand, baseCommand.name(), (BaseMainCommand) instance, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    public static Map<String, Command> getKnownCommands() {
        return knownCommands;
    }

    private static <T> T skip(T obj) {
        return skip(obj, null);
    }

    private static <T> T skip(T obj, T def) {
        return Strings.isEmpty(String.valueOf(obj)) ? def : obj;
    }
}
