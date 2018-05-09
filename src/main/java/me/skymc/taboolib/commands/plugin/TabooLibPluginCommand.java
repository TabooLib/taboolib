package me.skymc.taboolib.commands.plugin;

import com.google.common.base.Joiner;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.internal.InternalArgument;
import me.skymc.taboolib.commands.internal.InternalCommandExecutor;
import me.skymc.taboolib.commands.internal.InternalSubCommandExecutor;
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
public class TabooLibPluginCommand extends InternalCommandExecutor {

    public TabooLibPluginCommand() {
        listCommand();
        infoCommand();
        loadCommand();
        unloadCommand();
        reloadCommand();
    }

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TPLUGIN.COMMAND-TITLE");
    }

    void loadCommand() {
        registerSubCommandExecutor(new InternalSubCommandExecutor() {

            @Override
            public String getLabel() {
                return "load";
            }

            @Override
            public String getDescription() {
                return TLocale.asString("COMMANDS.TPLUGIN.LOAD.DESCRIPTION");
            }

            @Override
            public InternalArgument[] getArguments() {
                return new InternalArgument[]{new InternalArgument(TLocale.asString("COMMANDS.TPLUGIN.LOAD.ARGUMENTS.0"), true)};
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
                            TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LOAD.LOAD-FALL", name);
                        }
                    }
                }
            }
        });
    }

    void unloadCommand() {
        registerSubCommandExecutor(new InternalSubCommandExecutor() {

            @Override
            public String getLabel() {
                return "unload";
            }

            @Override
            public String getDescription() {
                return TLocale.asString("COMMANDS.TPLUGIN.UNLOAD.DESCRIPTION");
            }

            @Override
            public InternalArgument[] getArguments() {
                return new InternalArgument[]{new InternalArgument(TLocale.asString("COMMANDS.TPLUGIN.UNLOAD.ARGUMENTS.0"), true)};
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
                            TLocale.sendTo(sender, "COMMANDS.TPLUGIN.UNLOAD.UNLOAD-FALL", name);
                        }
                    }
                }
            }
        });
    }

    void reloadCommand() {
        registerSubCommandExecutor(new InternalSubCommandExecutor() {

            @Override
            public String getLabel() {
                return "reload";
            }

            @Override
            public String getDescription() {
                return TLocale.asString("COMMANDS.TPLUGIN.RELOAD.DESCRIPTION");
            }

            @Override
            public InternalArgument[] getArguments() {
                return new InternalArgument[]{new InternalArgument(TLocale.asString("COMMANDS.TPLUGIN.RELOAD.ARGUMENTS.0"), true)};
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
        });
    }

    void infoCommand() {
        registerSubCommandExecutor(new InternalSubCommandExecutor() {

            @Override
            public String getLabel() {
                return "info";
            }

            @Override
            public String getDescription() {
                return TLocale.asString("COMMANDS.TPLUGIN.INFO.DESCRIPTION");
            }

            @Override
            public InternalArgument[] getArguments() {
                return new InternalArgument[]{new InternalArgument(TLocale.asString("COMMANDS.TPLUGIN.INFO.ARGUMENTS.0"), true)};
            }

            @Override
            public void onCommand(CommandSender sender, Command command, String label, String[] args) {
                String name = ArrayUtils.arrayJoin(args, 0);
                Plugin plugin = PluginUtils.getPluginByName(name);
                if (plugin == null) {
                    TLocale.sendTo(sender, "COMMANDS.TPLUGIN.INFO.INVALID-PLUGIN", name);
                } else {
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
                }
            }
        });
    }

    void listCommand() {
        registerSubCommandExecutor(new InternalSubCommandExecutor() {

            @Override
            public String getLabel() {
                return "list";
            }

            @Override
            public String getDescription() {
                return TLocale.asString("COMMANDS.TPLUGIN.LIST.DESCRIPTION");
            }

            @Override
            public InternalArgument[] getArguments() {
                return new InternalArgument[0];
            }

            @Override
            public void onCommand(CommandSender sender, Command command, String label, String[] args) {
                List<String> pluginList = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
                TLocale.sendTo(sender, "COMMANDS.TPLUGIN.LIST.LIST-PLUGIN", String.valueOf(Bukkit.getPluginManager().getPlugins().length), Joiner.on(", ").join(pluginList));
            }
        });
    }
}