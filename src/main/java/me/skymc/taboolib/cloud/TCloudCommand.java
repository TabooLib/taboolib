package me.skymc.taboolib.cloud;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.cloud.expansion.Expansion;
import me.skymc.taboolib.commands.internal.BaseMainCommand;
import me.skymc.taboolib.commands.internal.BaseSubCommand;
import me.skymc.taboolib.commands.internal.TCommand;
import me.skymc.taboolib.commands.internal.plugin.TLibLocale;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandRegister;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.plugin.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Arrays;
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
            } else if (TCloudLoader.isExpansionExists(expansion)) {
                TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.EXPANSION-EXISTS", args[0]);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(TabooLib.instance(), () -> {
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.DOWNLOAD-START", args[0], expansion.getFile().getPath(), expansion.getLink());
                    FileUtils.download(expansion.getLink(), expansion.getFile());
                    TLocale.sendTo(sender, "COMMANDS.TCLOUD.DOWNLOAD.DOWNLOAD-SUCCESS", args[0]);
                    Bukkit.getScheduler().runTask(TabooLib.instance(), () -> PluginUtils.load(expansion.getName()));
                });
            }
        }
    };

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TCLOUD.COMMAND-TITLE");
    }
}
