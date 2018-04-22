package me.skymc.taboolib.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.team.TagManager;

/**
 * @author sky
 * @since 2018-03-19 23:13:35
 */
public class TagDeleteCommand extends SubCommand {

	/**
	 * @param sender
	 * @param args
	 */
	public TagDeleteCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 2) {
			MsgUtils.send(sender, "参数错误");
			return;
		}
		
		Player player = Bukkit.getPlayerExact(args[1]);
		if (player == null) {
			MsgUtils.send(sender, "玩家 &f" + args[1] + " &7不在线");
			return;
		}
		
		TagManager.getInst().removeData(player);
		if (sender instanceof Player) {
			MsgUtils.send(sender, "删除玩家 &f" + args[1] + " &7的称号数据");
		}
	}

}
