package io.izzel.taboolib.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * @Author 坏黑
 * @Since 2019-07-09 19:40
 */
public class Commands {

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
