package me.skymc.taboolib.cloud;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.cloud.expansion.Expansion;
import me.skymc.taboolib.cloud.expansion.ExpansionType;
import me.skymc.taboolib.commands.internal.BaseMainCommand;
import me.skymc.taboolib.commands.internal.BaseSubCommand;
import me.skymc.taboolib.commands.internal.TCommand;
import me.skymc.taboolib.commands.internal.plugin.TLibLocale;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandRegister;
import me.skymc.taboolib.common.util.SimpleIterator;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.plugin.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-09-30 19:17
 */
@TCommand(
        name = "taboolibcloud",
        permission = "taboolib.admin",
        aliases = "tcloud"
)
public class TCloudCommand extends BaseMainCommand {

    @CommandRegister
    BaseSubCommand refresh = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "refresh";
        }

        @Override
        public String getDescription() {
            return TLibLocale.description("TCLOUD", "REFRESH");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            TLocale.sendTo(sender, "COMMANDS.TCLOUD.REFRESH.SUCCESS");
            TCloudLoader.refresh();
        }
    };
    @CommandRegister(priority = 1)
    BaseSubCommand status = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "status";
        }

        @Override
        public String getDescription() {
            return TLibLocale.description("TCLOUD", "STATUS");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!TCloudLoader.isConnected()) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.STATUS.CONNECT-FAILED");
            } else {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.STATUS.STATUS", String.valueOf(TCloudLoader.getExpansionInternal().size() + TCloudLoader.getExpansionPlugins().size()), String.valueOf(TCloudLoader.getExpansionInternal().size()), String.valueOf(TCloudLoader.getExpansionPlugins().size()));
            }
        }
    };
    @CommandRegister(priority = 2)
    BaseSubCommand info = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "info";
        }

        @Override
        public String getDescription() {
            return TLibLocale.description("TCLOUD", "INFO");
        }

        @Override
        public CommandArgument[] getArguments() {
            return TLibLocale.arguments("TCLOUD", "INFO", 1);
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            Expansion expansion = TCloudLoader.getExpansion(args[0]);
            if (expansion == null) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.INFO.EXPANSION-NOT-FOUND", args[0]);
            } else {
                String builder = Arrays.stream(expansion.getAuthor()).map(author -> author + ", ").collect(Collectors.joining());
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.INFO.EXPANSION-INFO", expansion.getName(), builder.substring(0, builder.length() - 2), expansion.getVersion(), expansion.getDescription(), expansion.getLastUpdate(), expansion.getLastUpdateNote());
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.INFO.EXPANSION-INFO-DETAIL.0");
                Arrays.stream(expansion.getDetail()).forEach(detail -> TLocale.sendTo(sender, "COMMANDS.TCLOUD.INFO.EXPANSION-INFO-DETAIL.1", detail));
            }
        }
    };

    @CommandRegister(priority = 3)
    BaseSubCommand download = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "download";
        }

        @Override
        public String getDescription() {
            return TLibLocale.description("TCLOUD", "DOWNLOAD");
        }

        @Override
        public CommandArgument[] getArguments() {
            return TLibLocale.arguments("TCLOUD", "DOWNLOAD", 1);
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            Expansion expansion = TCloudLoader.getExpansion(args[0]);
            if (expansion == null) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.EXPANSION-NOT-FOUND", args[0]);
            } else if (!expansion.canUse()) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.EXPANSION-VERSION", args[0], String.valueOf(expansion.getDependVersion()));
            } else if (TCloudLoader.isExpansionExists(expansion)) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.EXPANSION-EXISTS", args[0]);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(TabooLib.instance(), () -> {
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.DOWNLOAD-START", args[0], expansion.getLink());
                    FileUtils.download(expansion.getLink(), expansion.getFile());
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.DOWNLOAD-SUCCESS", args[0]);
                });
            }
        }
    };

    @CommandRegister(priority = 4)
    BaseSubCommand update = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "update";
        }

        @Override
        public String getDescription() {
            return TLibLocale.description("TCLOUD", "UPDATE");
        }

        @Override
        public CommandArgument[] getArguments() {
            return TLibLocale.arguments("TCLOUD", "UPDATE", 1);
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            Expansion expansion = TCloudLoader.getExpansion(args[0]);
            if (expansion == null) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.UPDATE.EXPANSION-NOT-FOUND", args[0]);
            } else if (!TCloudLoader.isExpansionExists(expansion)) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.UPDATE.EXPANSION-NOT-EXISTS", args[0]);
            } else if (!expansion.canUpdate()) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.UPDATE.EXPANSION-NO-UPDATE", args[0]);
            } else if (!expansion.canUse()) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.UPDATE.EXPANSION-VERSION", args[0], String.valueOf(expansion.getDependVersion()));
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(TabooLib.instance(), () -> {
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.UPDATE.UPDATE-START", args[0], expansion.getVersion(), expansion.getLink());
                    FileUtils.download(expansion.getLink(), PluginUtils.getPluginFile(expansion.getName()));
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.UPDATE.UPDATE-SUCCESS", args[0]);
                });
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
            return TLibLocale.description("TCLOUD", "LIST");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    TLibLocale.argument("TCLOUD", "LIST", 0),
                    TLibLocale.argument("TCLOUD", "LIST", 1, false)
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            ExpansionType type;
            switch (args[0].toLowerCase()) {
                case "plugins":
                    type = ExpansionType.PLUGIN;
                    break;
                case "internal":
                    type = ExpansionType.INTERNAL;
                    break;
                default:
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.LIST.INVALID-TYPE.0");
                    return;
            }
            Map<String, Expansion> expansions = type == ExpansionType.PLUGIN ? TCloudLoader.getExpansionPlugins() : TCloudLoader.getExpansionInternal();
            int page = args.length < 2 ? 1 : NumberConversions.toInt(args[1]);
            int pageMax = (expansions.size() / 5) + ((expansions.size() % 5) == 0 ? 0 : 1);
            if (page < 1 || page > pageMax) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.LIST.INVALID-TYPE.1");
                return;
            }
            TLocale.sendTo(sender, "COMMANDS.TCLOUD.LIST.LIST-HEAD", type.name(), String.valueOf(page), String.valueOf(pageMax));
            int i = (page - 1) * 5;
            for (Map.Entry<String, Expansion> entry : new SimpleIterator(expansions).mapIterator((page - 1) * 5, page * 5)) {
                if (!TCloudLoader.isExpansionExists(entry.getValue())) {
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.LIST.LIST-EXPANSION.0", String.valueOf(++i), entry.getValue().getName(), Arrays.toString(entry.getValue().getAuthor()));
                } else if (entry.getValue().canUpdate()) {
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.LIST.LIST-EXPANSION.1", String.valueOf(++i), entry.getValue().getName(), Arrays.toString(entry.getValue().getAuthor()));
                } else {
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.LIST.LIST-EXPANSION.2", String.valueOf(++i), entry.getValue().getName(), Arrays.toString(entry.getValue().getAuthor()));
                }
            }
            TLocale.sendTo(sender, "COMMANDS.TCLOUD.LIST.LIST-BOTTOM");
        }
    };

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TCLOUD.COMMAND-TITLE");
    }
}
