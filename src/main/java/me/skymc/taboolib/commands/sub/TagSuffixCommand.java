package me.skymc.taboolib.commands.sub;

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
public class TagSuffixCommand extends SubCommand {

	/**
	 * @param sender
	 * @param args
	 */
	public TagSuffixCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 3) {
			MsgUtils.send(sender, "参数错误");
			return;
		}
		
		Player player = Bukkit.getPlayerExact(args[1]);
		if (player == null) {
			MsgUtils.send(sender, "玩家 &f" + args[1] + " &7不在线");
			return;
		}
		
		String value = getArgs(2).replace("&", "§");
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			value = PlaceholderAPI.setPlaceholders(player, value);
		}
		
		TagManager.getInst().setSuffix(player, value);
		if (sender instanceof Player) {
			MsgUtils.send(sender, "设置玩家 &f" + args[1] + " &7的后缀为 &f" + value);
		}
	}

}
