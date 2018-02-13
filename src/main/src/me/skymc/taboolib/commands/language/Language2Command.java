package me.skymc.taboolib.commands.language;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.string.language2.Language2Value;

/**
 * @author sky
 * @since 2018年2月13日 下午5:11:01
 */
public class Language2Command implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("§f");
			sender.sendMessage("§b§l----- §3§lTabooLib Commands §b§l-----");
			sender.sendMessage("§f");
			sender.sendMessage("§7 /language2 reload §f- §8重载语言库");
			sender.sendMessage("§7 /language2 send [玩家] [语言] <变量> §f- §8发送语言提示");
			sender.sendMessage("§f");
		}
		else if (args[0].equalsIgnoreCase("reload")) {
			MsgUtils.send(sender, "§7重载中..");
			long time = System.currentTimeMillis();
			Main.getExampleLangauge2().reload();
			MsgUtils.send(sender, "§7重载完成! 耗时: &f" + (System.currentTimeMillis() - time) + "ms");
		}
		else if (args[0].equalsIgnoreCase("send")) {
			if (args.length < 3) {
				MsgUtils.send(sender, "§4参数错误");
			}
			else {
				// 获取玩家
				Player player = Bukkit.getPlayerExact(args[1]);
				if (player == null) {
					MsgUtils.send(sender, "§4玩家不在线");
				}
				else {
					// 时间
					long time = System.currentTimeMillis();
					
					// 获取语言文件
					Language2Value value = Main.getExampleLangauge2().get(args[2]);
					
					// 如果有变量参数
					if (args.length > 3) {
						int i = 0;
						for (String variable : args[3].split("\\|")) {
							value.addPlaceholder("$" + i, variable);
							i++;
						}
					}
					
					// 发送信息
					value.send(player);
					
					// 如果发送者是玩家
					if (sender instanceof Player && ((Player) sender).getItemInHand().getType().equals(Material.COMMAND)) {
						MsgUtils.send(sender, "§7信息已发送, 本次计算耗时: &f" + (System.currentTimeMillis() - time) + "ms");
					}
				}
			}
		}
		return true;
	}
}
