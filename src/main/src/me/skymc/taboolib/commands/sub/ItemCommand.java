package me.skymc.taboolib.commands.sub;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.other.NumberUtils;

public class ItemCommand extends SubCommand {
	
	/**
	 * /TabooLib item 物品 玩家 数量
	 * 
	 * @param sender
	 * @param args
	 */
	public ItemCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (args.length < 2) {
			MsgUtils.send(sender, "请输入正确的物品名称");
			setReturn(false);
		}
		else {
			if (ItemUtils.getCacheItem(args[1]) == null) {
				MsgUtils.send(sender, "物品 &f" + args[1] + "&7 不存在");
				setReturn(false);
				return;
			}
			
			Player player;
			Integer amount = 1;
			ItemStack item = ItemUtils.getCacheItem(args[1]).clone();
			
			if (args.length > 2) {
				player = Bukkit.getPlayerExact(args[2]);
				if (player == null) {
					MsgUtils.send(sender, "玩家 &f" + args[2] + "&7 不在线");
					setReturn(false);
					return;
				}
			}
			else if (sender instanceof Player) {
				player = (Player) sender;
			}
			else {
				MsgUtils.send(sender, "后台不允许这么做");
				setReturn(false);
				return;
			}
			
			if (args.length > 3) {
				amount = NumberUtils.getInteger(args[3]);
				if (amount < 1) {
					MsgUtils.send(sender, "数量必须大于0");
					setReturn(false);
					return;
				}
			}
			item.setAmount(amount);
			
			HashMap<Integer, ItemStack> map = player.getInventory().addItem(item);
			if (map.size() > 0) {
				player.getWorld().dropItem(player.getLocation(), item);
			}
			
			MsgUtils.send(sender, "物品已发送至玩家 &f" + player.getName() + " &7的背包中");
			setReturn(true);
		}
	}
}
