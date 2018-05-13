package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.team.TagAPI;
import me.skymc.taboolib.team.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author sky
 * @since 2018-03-19 23:13:35
 */
public class TagDeleteCommand extends SubCommand {

    public TagDeleteCommand(CommandSender sender, String[] args) {
        super(sender, args);
        if (args.length < 2) {
            TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
            return;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.PLAYERTAG.INVALID-PLAYER", args[1]);
            return;
        }

        TagManager.getInst().unloadData(player);
        TagAPI.removePlayerDisplayName(player);

        if (sender instanceof Player) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.PLAYERTAG.SUCCESS-DELETE", args[1]);
        }
    }
}
