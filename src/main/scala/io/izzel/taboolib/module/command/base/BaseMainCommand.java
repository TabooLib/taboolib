package io.izzel.taboolib.module.command.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-07 21:38
 */
public abstract class BaseMainCommand implements CommandExecutor, TabExecutor {

    private PluginCommand registerCommand;
    private List<Class<?>> linkClasses = new CopyOnWriteArrayList<>();
    private List<BaseSubCommand> subCommands = new CopyOnWriteArrayList<>();

    public static BaseMainCommand createCommandExecutor(String command, BaseMainCommand baseMainCommand) {
        Preconditions.checkArgument(Bukkit.getPluginCommand(command) != null, "PluginCommand \"" + command + "\" not found");
        Preconditions.checkArgument(baseMainCommand != null, "Executor cannot be null");
        Preconditions.checkArgument(baseMainCommand.getClass() != BaseMainCommand.class, "Executor can not be \"BaseMainCommand.class\"");
        baseMainCommand.setRegisterCommand(Bukkit.getPluginCommand(command));
        baseMainCommand.getRegisterCommand().setExecutor(baseMainCommand);
        baseMainCommand.getRegisterCommand().setTabCompleter(baseMainCommand);
        baseMainCommand.getLinkClasses().add(baseMainCommand.getClass());
        baseMainCommand.disguisedPlugin();
        loadCommandRegister(baseMainCommand);
        return baseMainCommand;
    }

    public static void loadCommandRegister(BaseMainCommand baseMainCommand) {
        List<Method> methods = new ArrayList<>();
        List<CommandField> fields = new ArrayList<>();
        baseMainCommand.getLinkClasses().forEach(clazz -> java.util.Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.getAnnotation(SubCommand.class) != null).forEach(methods::add));
        if (methods.size() > 0) {
            methods.sort(Comparator.comparingDouble(a -> a.getAnnotation(SubCommand.class).priority()));
            methods.forEach(method -> {
                BaseSubCommand subCommand = null;
                try {
                    method.setAccessible(true);
                    // lite parameter
                    if (Arrays.equals(method.getParameterTypes(), new Class[] {CommandSender.class, String[].class})) {
                        subCommand = new BaseSubCommand() {
                            @Override
                            public void onCommand(CommandSender sender, Command command, String label, String[] args) {
                                try {
                                    method.invoke(baseMainCommand, sender, args);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        };
                    }
                    // fully parameter
                    else if (Arrays.equals(method.getParameterTypes(), new Class[] {CommandSender.class, Command.class, String.class, String[].class})) {
                        subCommand = new BaseSubCommand() {
                            @Override
                            public void onCommand(CommandSender sender, Command command, String label, String[] args) {
                                try {
                                    method.invoke(baseMainCommand, sender, command, label, args);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        };
                    }
                    if (subCommand != null) {
                        subCommand.setLabel(method.getName());
                        subCommand.setAnnotation(method.getAnnotation(SubCommand.class));
                        baseMainCommand.registerSubCommand(subCommand);
                    }
                } catch (Throwable ignored) {
                }
            });
        }
        baseMainCommand.getLinkClasses().forEach(clazz -> java.util.Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.getAnnotation(SubCommand.class) != null && field.getType().equals(BaseSubCommand.class)).forEach(field -> fields.add(new CommandField(field, clazz))));
        if (fields.size() > 0) {
            fields.sort(Comparator.comparingDouble(commandField -> commandField.getField().getAnnotation(SubCommand.class).priority()));
            fields.forEach(commandField -> {
                try {
                    commandField.getField().setAccessible(true);
                    BaseSubCommand subCommand = (BaseSubCommand) commandField.getField().get(commandField.getParent().newInstance());
                    subCommand.setLabel(commandField.getField().getName());
                    subCommand.setAnnotation(commandField.getField().getAnnotation(SubCommand.class));
                    baseMainCommand.registerSubCommand(subCommand);
                } catch (Throwable ignored) {
                }
            });
        }
        if (methods.size() + fields.size() > 0) {
            TabooLibAPI.debug("Registered " + (methods.size() + fields.size()) + " sub-command with " + baseMainCommand.getRegisterCommand().getName() + " (" + baseMainCommand.getRegisterCommand().getPlugin().getName() + ")");
        }
    }

    public void setRegisterCommand(PluginCommand registerCommand) {
        this.registerCommand = registerCommand;
    }

    public PluginCommand getRegisterCommand() {
        return registerCommand;
    }

    public List<Class<?>> getLinkClasses() {
        return linkClasses;
    }

    public List<BaseSubCommand> getSubCommands() {
        return subCommands;
    }

    public void registerSubCommand(BaseSubCommand subCommand) {
        if (subCommand != null) {
            Preconditions.checkArgument(subCommand.getLabel() != null, "Command label can not be null");
            Preconditions.checkArgument(subCommand.getArguments() != null, "Command arguments can not be null");
        }
        subCommands.add(subCommand);
    }

    public void onCommandHelp(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(getEmptyLine());
        sender.sendMessage(getCommandTitle());
        sender.sendMessage(getEmptyLine());
        subCommands.stream().filter(subCommands -> !hideInHelp(subCommands) && hasPermission(sender, subCommands)).map(subCommand -> subCommand == null ? getEmptyLine() : subCommand.getCommandString(label)).forEach(sender::sendMessage);
        sender.sendMessage(getEmptyLine());
    }

    public String getCommandTitle() {
        return "§e§l----- §6§l" + registerCommand.getPlugin().getName() + " Commands §e§l-----";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1) {
            List<String> label = Lists.newArrayList();
            subCommands.stream().filter(subCommand -> !hideInHelp(subCommand) && hasPermission(commandSender, subCommand)).forEach(l -> {
                label.add(l.getLabel());
                label.addAll(Lists.newArrayList(l.getAliases()));
            });
            return label.stream().filter(l -> args[0].isEmpty() || l.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        for (BaseSubCommand subCommand : subCommands) {
            Argument[] arguments = subCommand.getArguments();
            if (args[0].equalsIgnoreCase(subCommand.getLabel()) && args.length - 1 <= arguments.length) {
                CommandTab commandTab = arguments[args.length - 2].getTab();
                if (commandTab != null) {
                    return commandTab.run().stream().filter(l -> args[args.length - 1].isEmpty() || l.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
                }
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            onCommandHelp(sender, command, label, args);
        } else {
            for (BaseSubCommand subCommand : subCommands) {
                if (subCommand == null || !(args[0].equalsIgnoreCase(subCommand.getLabel()) || java.util.Arrays.stream(subCommand.getAliases()).anyMatch(args[0]::equalsIgnoreCase)) || !hasPermission(sender, subCommand)) {
                    continue;
                }
                if (!isConfirmType(sender, subCommand.getType())) {
                    TLocale.sendTo(sender, "COMMANDS.INTERNAL.TYPE-ERROR", args[0], TLocale.asString("COMMANDS.INTERNAL.TYPE-" + subCommand.getType()));
                    return true;
                }
                String[] subCommandArgs = removeFirst(args);
                if (subCommand.isParameterConform(subCommandArgs)) {
                    subCommand.onCommand(sender, command, label, subCommand.ignoredLabel() ? subCommandArgs : args);
                } else {
                    TLocale.sendTo(sender, "COMMANDS.INTERNAL.ERROR-USAGE", args[0], subCommand.getCommandString(label));
                }
                return true;
            }
            new BukkitRunnable() {

                @Override
                public void run() {
                    List<BaseSubCommand> commandCompute = subCommands.stream().filter(x -> x != null && hasPermission(sender, x)).sorted((b, a) -> Double.compare(Strings.similarDegree(args[0], a.getLabel()), Strings.similarDegree(args[0], b.getLabel()))).collect(Collectors.toList());
                    if (commandCompute.size() > 0) {
                        TLocale.sendTo(sender, "COMMANDS.INTERNAL.ERROR-COMMAND", args[0], commandCompute.get(0).getCommandString(label).trim());
                    }
                }
            }.runTaskAsynchronously(TabooLib.getPlugin());
        }
        return true;
    }

    @Override
    public String toString() {
        return "registerCommand=" + "BaseMainCommand{" + registerCommand + ", linkClasses=" + linkClasses + ", subCommands=" + subCommands + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseMainCommand)) {
            return false;
        }
        BaseMainCommand that = (BaseMainCommand) o;
        return Objects.equals(getLinkClasses(), that.getLinkClasses()) && Objects.equals(getRegisterCommand(), that.getRegisterCommand()) && Objects.equals(getSubCommands(), that.getSubCommands());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegisterCommand(), getLinkClasses(), getSubCommands());
    }

    // *********************************
    //
    //        Private Methods
    //
    // *********************************

    private String getEmptyLine() {
        return Version.isAfter(Version.v1_8) ? "" : "~";
    }

    private boolean isConfirmType(CommandSender sender, CommandType commandType) {
        return commandType == CommandType.ALL
                || (sender instanceof Player && commandType == CommandType.PLAYER)
                || (sender instanceof ConsoleCommandSender && commandType == CommandType.CONSOLE);
    }

    private void disguisedPlugin() {
        try {
            linkClasses.forEach(clazz -> disguisedPlugin(clazz, registerCommand.getPlugin()));
        } catch (Throwable ignored) {
        }
    }

    private void disguisedPlugin(Class<?> targetClass, Plugin plugin) {
        try {
            Field pluginField = targetClass.getClassLoader().getClass().getDeclaredField("plugin");
            pluginField.setAccessible(true);
            pluginField.set(targetClass.newInstance(), plugin);
        } catch (Exception ignored) {
        }
    }

    private boolean hideInHelp(BaseSubCommand baseSubCommand) {
        return baseSubCommand != null && baseSubCommand.hideInHelp();
    }

    private boolean hasPermission(CommandSender sender, BaseSubCommand baseSubCommand) {
        return baseSubCommand == null || Strings.isEmpty(baseSubCommand.getPermission()) || sender.hasPermission(baseSubCommand.getPermission());
    }

    private String[] removeFirst(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        List<String> list = ArrayUtil.asList(args);
        list.remove(0);
        return list.toArray(new String[0]);
    }
}
