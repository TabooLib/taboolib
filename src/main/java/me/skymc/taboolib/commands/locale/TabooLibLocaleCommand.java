package me.skymc.taboolib.commands.locale;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author sky
 * @since 2018-04-22 14:36:28
 */
public class TabooLibLocaleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String label, String[] args) {
        if (args.length == 0) {
            TLocale.sendTo(sender, "COMMANDS.TLOCALE.HELP", label);
        } else if (args[0].equalsIgnoreCase("send")) {
            send(sender, args);
        } else if (args[0].equalsIgnoreCase("reload")) {
            reload(sender);
        } else {
            TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
        }
        return true;
    }

    void send(CommandSender sender, String[] args) {
        if (args.length < 3) {
            TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
            return;
        }

        long time = System.currentTimeMillis();
        List<Player> target = new ArrayList<>();
        if (args[1].equalsIgnoreCase("all")) {
            target.addAll(Bukkit.getOnlinePlayers());
        } else {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                TLocale.sendTo(sender, "COMMANDS.TLOCALE.INVALID-PLAYER", args[1]);
                return;
            }
            target.add(player);
        }

        if (args.length > 3) {
            String[] vars = new String[args.length - 3];
            IntStream.range(3, args.length).forEach(i -> vars[i - 3] = args[i]);
            target.forEach(x -> TLocale.sendTo(x, args[2], vars));
        } else {
            target.forEach(x -> TLocale.sendTo(x, args[2]));
        }

        if (sender instanceof Player) {
            TLocale.sendTo(sender, "COMMANDS.TLOCALE.SUCCESS-SEND", String.valueOf(System.currentTimeMillis() - time));
        }
    }

    void reload(CommandSender sender) {
        TLocale.reload();
        TLocale.sendTo(sender, "COMMANDS.TLOCALE.SUCCESS-RELOAD");
    }

}
