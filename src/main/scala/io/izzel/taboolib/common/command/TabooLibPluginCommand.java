package io.izzel.taboolib.common.command;

import com.google.common.base.Joiner;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.command.base.BaseCommand;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import io.izzel.taboolib.module.command.base.Argument;
import io.izzel.taboolib.module.command.base.SubCommand;
import io.izzel.taboolib.util.plugin.PluginLoadState;
import io.izzel.taboolib.util.plugin.PluginLoadStateType;
import io.izzel.taboolib.util.plugin.PluginUnloadState;
import io.izzel.taboolib.util.plugin.PluginUtils;
import io.izzel.taboolib.util.ArrayUtil;
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
@BaseCommand(
        name = "tPlugin",
        permission = "taboolib.admin"
)
public class TabooLibPluginCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TPLUGIN.COMMAND-TITLE");
    }

    @SubCommand(priority = 1)
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
        public Argument[] getArguments() {
            return new Argument[] {new Argument(TLocale.asString("COMMANDS.TPLUGIN.LOAD.ARGUMENTS.0"), true)};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtil.arrayJoin(args, 0);
            if (PluginUtils.getPluginByName(name) != null) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.INVALID-PLUGIN", name, name + " already loaded!");
            } else {
                PluginLoadState loadState;
                try {
                    loadState = PluginUtils.load(name);
                } catch (Exception e) {
                    loadState = new PluginLoadState(PluginLoadStateType.INVALID_PLUGIN, e.toString());
                }
                switch (loadState.getStateType()) {
                    case INVALID_DESCRIPTION: {
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.INVALID-DESCRIPTION");
                        break;
                    }
                    case INVALID_PLUGIN: {
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.INVALID-PLUGIN", name, loadState.getMessage());
                        break;
                    }
                    case FILE_NOT_FOUND: {
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.FILE-NOT-FOUND", name);
                        break;
                    }
                    default:
                        TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.LOAD-SUCCESS", name);
                }
            }
        }
    };

    @SubCommand(priority = 2)
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
        public Argument[] getArguments() {
            return new Argument[] {new Argument(TLocale.asString("COMMANDS.TPLUGIN.UNLOAD.ARGUMENTS.0"), true, () -> {
                return java.util.Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList());
            })};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtil.arrayJoin(args, 0);
            Plugin plugin = PluginUtils.getPluginByName(name);
            if (plugin == null) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.INVALID-PLUGIN", name);
            } else if (PluginUtils.isIgnored(plugin)) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.INVALID-PLUGIN-IGNORED", name);
            } else {
                PluginUnloadState unloadState;
                try {
                    unloadState = PluginUtils.unload(plugin);
                } catch (Exception e) {
                    unloadState = new PluginUnloadState(true, e.toString());
                }
                if (unloadState.isFailed()) {
                    TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.UNLOAD-FAIL", name, unloadState.getMessage());
                } else {
                    TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.UNLOAD-SUCCESS", name);
                }
            }
        }
    };

    @SubCommand(priority = 3)
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
        public Argument[] getArguments() {
            return new Argument[] {new Argument(TLocale.asString("COMMANDS.TPLUGIN.RELOAD.ARGUMENTS.0"), true, () -> {
                return Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList());
            })};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtil.arrayJoin(args, 0);
            Plugin plugin = PluginUtils.getPluginByName(name);
            if (plugin == null) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.RELOAD.INVALID-PLUGIN", name);
            } else if (PluginUtils.isIgnored(plugin)) {
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.RELOAD.INVALID-PLUGIN-IGNORED", name);
            } else {
                TabooLibAPI.dispatchCommand(sender, "taboolibplugin unload " + plugin.getName());
                TabooLibAPI.dispatchCommand(sender, "taboolibplugin load " + plugin.getName());
            }
        }
    };

    @SubCommand(priority = 4)
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
        public Argument[] getArguments() {
            return new Argument[] {new Argument(TLocale.asString("COMMANDS.TPLUGIN.INFO.ARGUMENTS.0"), true, () -> {
                return Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList());
            })};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            String name = ArrayUtil.arrayJoin(args, 0);
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
                            String.valueOf(plugin.getDescription().getCommands() == null ? "" : plugin.getDescription().getCommands().keySet()));
                } catch (Exception e) {
                    TLocale.sendTo(sender, "COMMANDS.TPLUGIN.INFO.INVALID-DESCRIPTION", name, e.getMessage());
                }
            }
        }
    };

    @SubCommand(priority = 5)
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
        public Argument[] getArguments() {
            return new Argument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            List<String> pluginList = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(PluginUtils::getFormattedName).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
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