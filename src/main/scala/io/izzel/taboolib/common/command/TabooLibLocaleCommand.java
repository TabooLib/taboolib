package io.izzel.taboolib.common.command;

import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleLoader;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import io.izzel.taboolib.module.command.base.BaseCommand;
import io.izzel.taboolib.module.command.base.Argument;
import io.izzel.taboolib.module.command.base.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author sky
 * @since 2018-04-22 14:36:28
 */
@BaseCommand(
        name = "tLocale",
        permission = "taboolib.admin"
)
public class TabooLibLocaleCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TLOCALE.COMMAND-TITLE");
    }

    @SubCommand
    BaseSubCommand send = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "send";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TLOCALE.SEND.DESCRIPTION");
        }

        @Override
        public Argument[] getArguments() {
            return new Argument[] {
                    new Argument(TLocale.asString("COMMANDS.TLOCALE.SEND.ARGUMENTS.0")),
                    new Argument(TLocale.asString("COMMANDS.TLOCALE.SEND.ARGUMENTS.1")),
                    new Argument(TLocale.asString("COMMANDS.TLOCALE.SEND.ARGUMENTS.2"), false)
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            long time = System.currentTimeMillis();

            List<CommandSender> target = new ArrayList<>();
            if (args[0].equalsIgnoreCase("all")) {
                target.add(Bukkit.getConsoleSender());
                target.addAll(Bukkit.getOnlinePlayers());
            } else if (args[0].equalsIgnoreCase("players")) {
                target.addAll(Bukkit.getOnlinePlayers());
            } else if (args[0].equalsIgnoreCase("console")) {
                target.add(Bukkit.getConsoleSender());
            } else {
                Player player = Bukkit.getPlayerExact(args[0]);
                if (player == null) {
                    TLocale.sendTo(sender, "COMMANDS.TLOCALE.SEND.INVALID-PLAYER", args[0]);
                    return;
                }
                target.add(player);
            }

            String[] arguments;
            if (args.length > 2) {
                arguments = new String[args.length - 2];
                IntStream.range(2, args.length).forEach(i -> arguments[i - 2] = args[i]);
            } else {
                arguments = new String[0];
            }

            /*
             * 使用命令发送其他插件文本
             * /tlocale send BlackSKY testPlugin:message
             */
            if (args[1].contains(":")) {
                String[] path = args[1].split(":");
                Plugin plugin = Bukkit.getPluginManager().getPlugin(path[0]);
                if (plugin == null || !TLocaleLoader.isLocaleLoaded(plugin)) {
                    TLocale.sendTo(sender, "COMMANDS.TLOCALE.SEND.INVALID-PLUGIN", path[0]);
                    return;
                }
                if (path.length == 1) {
                    TLocale.sendTo(sender, "COMMANDS.TLOCALE.SEND.INVALID-PATH", args[1]);
                    return;
                }
                target.forEach(x -> TLocaleLoader.sendTo(plugin, path[1], x, arguments));
            } else {
                target.forEach(x -> TLocale.sendTo(x, args[1], arguments));
            }

            if (sender instanceof Player && ((Player) sender).getItemInHand().getType().name().contains("COMMAND")) {
                TLocale.sendTo(sender, "COMMANDS.TLOCALE.SEND.SUCCESS-SEND", String.valueOf(System.currentTimeMillis() - time));
            }
        }
    };
}
