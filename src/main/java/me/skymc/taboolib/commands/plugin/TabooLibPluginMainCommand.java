package me.skymc.taboolib.commands.plugin;

import com.google.common.base.Joiner;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.internal.BaseMainCommand;
import me.skymc.taboolib.commands.internal.BaseSubCommand;
import me.skymc.taboolib.commands.internal.ISubCommand;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandRegister;
import me.skymc.taboolib.plugin.PluginUtils;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-07 20:14
 */
public class TabooLibPluginMainCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TPLUGIN.COMMAND-TITLE");
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1) {
            return getSubCommands().stream().filter(internalCommandExecutor -> internalCommandExecutor != null && (args[0].isEmpty() || internalCommandExecutor.getLabel().toLowerCase().startsWith(args[0].toLowerCase()))).map(ISubCommand::getLabel).collect(Collectors.toList());
        } else if (args.length > 1 && isPluginCommand(args[0])) {
            return Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(x -> !PluginUtils.isIgnored(x)).collect(Collectors.toList()).stream().filter(plugin -> args[1].isEmpty() || plugin.getName().toLowerCase().startsWith(args[1].toLowerCase())).map(Plugin::getName).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @CommandRegister(priority = 1)
    BaseSubCommand load = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "load";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TPLUGIN.LOAD.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{new CommandArgument(TLocale.asString("COMMANDS.TPLUGIN.LOAD.ARGUMENTS.0"), true)};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtils.arrayJoin(args, 0);
            if (PluginUtils.getPluginByName(name) != null) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.INVALID-PLUGIN", name);
            } else {
                switch (PluginUtils.load(name)) {
                    case "loaded": {
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.LOAD-SUCCESS", name);
                        break;
                    }
                    default: {
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.LOAD-FAIL", name);
                    }
                }
            }
        }
    };

    @CommandRegister(priority = 2)
    BaseSubCommand unload = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "unload";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TPLUGIN.UNLOAD.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{new CommandArgument(TLocale.asString("COMMANDS.TPLUGIN.UNLOAD.ARGUMENTS.0"), true)};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtils.arrayJoin(args, 0);
            Plugin plugin = PluginUtils.getPluginByName(name);
            if (plugin == null) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.INVALID-PLUGIN", name);
            } else if (PluginUtils.isIgnored(plugin)) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.INVALID-PLUGIN-IGNORED", name);
            } else {
                switch (PluginUtils.unload(plugin)) {
                    case "unloaded": {
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.UNLOAD-SUCCESS", name);
                        break;
                    }
                    default: {
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.UNLOAD-FAIL", name);
                    }
                }
            }
        }
    };

    @CommandRegister(priority = 3)
    BaseSubCommand reload = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "reload";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TPLUGIN.RELOAD.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{new CommandArgument(TLocale.asString("COMMANDS.TPLUGIN.RELOAD.ARGUMENTS.0"), true)};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtils.arrayJoin(args, 0);
            Plugin plugin = PluginUtils.getPluginByName(name);
            if (plugin == null) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.RELOAD.INVALID-PLUGIN", name);
            } else if (PluginUtils.isIgnored(plugin)) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.RELOAD.INVALID-PLUGIN-IGNORED", name);
            } else {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.RELOAD.TRY-RELOAD");
                PluginUtils.reload(plugin);
            }
        }
    };

    @CommandRegister(priority = 4)
    BaseSubCommand info = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "info";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TPLUGIN.INFO.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{new CommandArgument(TLocale.asString("COMMANDS.TPLUGIN.INFO.ARGUMENTS.0"), true)};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtils.arrayJoin(args, 0);
            Plugin plugin = PluginUtils.getPluginByName(name);
            if (plugin == null) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.INFO.INVALID-PLUGIN", name);
            } else {
                try {
                    TLocale.sendTo(sender, "COMMANDS.TPLUGIN.INFO.INFO-PLUGIN",
                            plugin.getName(),
                            String.valueOf(plugin.getDescription().getDescription()),
                            String.valueOf(plugin.getDescription().getAuthors()),
                            String.valueOf(plugin.getDescription().getDepend()),
                            String.valueOf(plugin.getDescription().getSoftDepend()),
                            String.valueOf(plugin.getDescription().getMain()),
                            String.valueOf(plugin.getDescription().getVersion()),
                            String.valueOf(plugin.getDescription().getWebsite()),
                            String.valueOf(plugin.getDescription().getCommands().keySet()));
                } catch (Exception ignored) {
                    TLocale.sendTo(sender, "COMMANDS.TPLUGIN.INFO.INVALID-PLUGIN", name);
                }
            }
        }
    };

    @CommandRegister(priority = 5)
    BaseSubCommand list = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "list";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TPLUGIN.LIST.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            List<String> pluginList = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
            TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LIST.LIST-PLUGIN", String.valueOf(Bukkit.getPluginManager().getPlugins().length), Joiner.on(", ").join(pluginList));
        }
    };

    // *********************************
    //
    //         Private Methods
    //
    // *********************************

    private boolean isPluginCommand(String label) {
        return "info".equalsIgnoreCase(label) || "load".equalsIgnoreCase(label) || "unload".equalsIgnoreCase(label) || "reload".equalsIgnoreCase(label);
    }
}