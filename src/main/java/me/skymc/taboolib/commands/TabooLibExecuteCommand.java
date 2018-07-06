package me.skymc.taboolib.commands;

import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.commands.internal.BaseMainCommand;
import me.skymc.taboolib.commands.internal.BaseSubCommand;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandRegister;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * @Author sky
 * @Since 2018-07-04 21:32
 */
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
            player.chat(ArrayUtils.arrayJoin(args, 1));
        }
    };

    @CommandRegister(priority = 1)
    BaseSubCommand command = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "command";
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
                dispatchCommand(Bukkit.getConsoleSender(), ArrayUtils.arrayJoin(args, 1));
                return;
            }
            Player player = Bukkit.getPlayerExact(args[0]);
            if (player == null) {
                TLocale.sendTo(sender, "INVALID-TARGET-NOT-FOUND", args[0]);
                return;
            }
            dispatchCommand(player, ArrayUtils.arrayJoin(args, 1));
        }
    };

    public static boolean dispatchCommand(CommandSender sender, String command) {
        try {
            if ((sender instanceof Player)) {
                PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent((Player) sender, "/" + command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled() || Strings.isBlank(e.getMessage()) || !e.getMessage().startsWith("/")) {
                    return false;
                }
                return Bukkit.dispatchCommand(e.getPlayer(), e.getMessage().substring(1));
            } else {
                ServerCommandEvent e = new ServerCommandEvent(sender, command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled() || Strings.isBlank(e.getCommand())) {
                    return false;
                }
                return Bukkit.dispatchCommand(e.getSender(), e.getCommand());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
