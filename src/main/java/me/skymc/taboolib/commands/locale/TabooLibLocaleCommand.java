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
            sender.sendMessage("§f");
            sender.sendMessage("§b§l----- §3§lTabooLibLoacle Commands §b§l-----");
            sender.sendMessage("§f");
            sender.sendMessage("§f /tloacle send §8[§7玩家/ALL§8] §8[§7语言§8] §8<§7变量§8> §6- §e发送语言提示");
            sender.sendMessage("§f /tloacle reload §6- §e重载语言库");
            sender.sendMessage("§f");
        } else if (args[0].equalsIgnoreCase("send")) {
            send(sender, args);
        } else if (args[0].equalsIgnoreCase("reload")) {
            reload(sender);
        } else {
            MsgUtils.send(sender, "§4参数错误");
        }
        return true;
    }

    void send(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MsgUtils.send(sender, "§4参数错误");
            return;
        }

        List<Player> target = new ArrayList<>();
        if (args[1].equalsIgnoreCase("all")) {
            target.addAll(Bukkit.getOnlinePlayers());
        } else {
            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                MsgUtils.send(sender, "§4玩家不在线");
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
            MsgUtils.send(sender, "§7信息已发送");
        }
    }

    void reload(CommandSender sender) {
        TLocale.reload();
        MsgUtils.send(sender, "§7重载完成");
    }

}
