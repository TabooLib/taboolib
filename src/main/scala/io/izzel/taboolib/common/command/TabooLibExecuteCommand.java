package io.izzel.taboolib.common.command;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import io.izzel.taboolib.module.command.base.BaseCommand;
import io.izzel.taboolib.module.command.base.CommandArgument;
import io.izzel.taboolib.module.command.base.CommandRegister;
import io.izzel.taboolib.util.ArrayUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @Author sky
 * @Since 2018-07-04 21:32
 */
@BaseCommand(
        name = "taboolibexecute",
        aliases = {"texecute"},
        permission = "taboolib.admin"
)
public class TabooLibExecuteCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TEXECUTE.COMMAND-TITLE");
    }

    @CommandRegister(priority = 1)
    BaseSubCommand chat = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "chat";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TEXECUTE.CHAT.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TEXECUTE.CHAT.ARGUMENTS.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TEXECUTE.CHAT.ARGUMENTS.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            Player player = Bukkit.getPlayerExact(args[0]);
            if (player == null) {
                TLocale.sendTo(sender, "INVALID-PLAYER-OFFLINE", args[0]);
                return;
            }
            player.chat(ArrayUtil.arrayJoin(args, 1));
        }
    };

    @CommandRegister(priority = 1)
    BaseSubCommand command = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "command";
        }

        @Override
        public String[] getAliases() {
            return new String[] {"cmd"};
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TEXECUTE.COMMAND.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TEXECUTE.COMMAND.ARGUMENTS.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TEXECUTE.COMMAND.ARGUMENTS.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args[0].equalsIgnoreCase("console")) {
                TabooLibAPI.dispatchCommand(Bukkit.getConsoleSender(), ArrayUtil.arrayJoin(args, 1));
                return;
            }
            Player player = Bukkit.getPlayerExact(args[0]);
            if (player == null) {
                TLocale.sendTo(sender, "INVALID-TARGET-NOT-FOUND", args[0]);
                return;
            }
            TabooLibAPI.dispatchCommand(player, ArrayUtil.arrayJoin(args, 1));
        }
    };

    @CommandRegister(priority = 2)
    BaseSubCommand commandAsOp = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "commandAsOp";
        }

        @Override
        public String[] getAliases() {
            return new String[] {"op"};
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TEXECUTE.COMMAND-AS-OP.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TEXECUTE.COMMAND-AS-OP.ARGUMENTS.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TEXECUTE.COMMAND-AS-OP.ARGUMENTS.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args[0].equalsIgnoreCase("console")) {
                TabooLibAPI.dispatchCommand(Bukkit.getConsoleSender(), ArrayUtil.arrayJoin(args, 1));
                return;
            }
            Player player = Bukkit.getPlayerExact(args[0]);
            if (player == null) {
                TLocale.sendTo(sender, "INVALID-TARGET-NOT-FOUND", args[0]);
                return;
            }
            boolean isOp = player.isOp();
            player.setOp(true);
            try {
                TabooLibAPI.dispatchCommand(player, ArrayUtil.arrayJoin(args, 1));
            } catch (Exception ignored) {
            }
            player.setOp(isOp);
        }
    };
}