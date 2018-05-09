package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.team.TagManager;

/**
 * @author sky
 * @since 2018-03-19 23:13:35
 */
public class TagPrefixCommand extends SubCommand {

	public TagPrefixCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 3) {
			TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
			return;
		}
		
		Player player = Bukkit.getPlayerExact(args[1]);
		if (player == null) {
			TLocale.sendTo(sender, "COMMANDS.TABOOLIB.PLAYERTAG.INVALID-PLAYER", args[1]);
			return;
		}
		
		String value = getArgs(2).replace("&", "§");
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			value = PlaceholderAPI.setPlaceholders(player, value);
		}
		
		TagManager.getInst().setPrefix(player, value);
		if (sender instanceof Player) {
			TLocale.sendTo(sender, "COMMANDS.TABOOLIB.PLAYERTAG.SUCCESS-PREFIX-SET", args[1], value);
		}
	}

}
