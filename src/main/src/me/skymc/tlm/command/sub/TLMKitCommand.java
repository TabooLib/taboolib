package me.skymc.tlm.command.sub;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.tlm.TLM;
import me.skymc.tlm.module.TabooLibraryModule;
import me.skymc.tlm.module.sub.ModuleKits;

/**
 * @author sky
 * @since 2018年2月18日 下午2:53:58
 */
public class TLMKitCommand extends SubCommand {

	/**
	 * @param sender
	 * @param args
	 */
	public TLMKitCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (TabooLibraryModule.getInst().valueOf("Kits") == null) {
			TLM.getInst().getLanguage().get("KIT-DISABLE").send(sender);
			return;
		}

		// 获取模块
		ModuleKits moduleKits = (ModuleKits) TabooLibraryModule.getInst().valueOf("Kits");
		
		// 判断命令
		if (args.length == 1) {
			TLM.getInst().getLanguage().get("KIT-EMPTY").send(sender);
			return;
		}
		
		else if (args[1].equalsIgnoreCase("list")) {
			// 判断权限
			if (!sender.hasPermission("taboolib.kit.list")) {
				TLM.getInst().getLanguage().get("NOPERMISSION-KIT-LIST").send(sender);
				return;
			}
			else {
				TLM.getInst().getLanguage().get("KIT-LIST")
						.addPlaceholder("$kits", moduleKits.getConfig().getConfigurationSection("Kits").getKeys(false).toString())
						.send(sender);
			}
		}
		
		else if (args[1].equalsIgnoreCase("reward")) {
			// 判断权限
			if (!sender.hasPermission("taboolib.kit.reward")) {
				TLM.getInst().getLanguage().get("NOPERMISSION-KIT-REWARD").send(sender);
				return;
			}
			
			// 检查礼包
			if (args.length < 3) {
				TLM.getInst().getLanguage().get("KIT-NAME").send(sender);
				return;
			}
			
			// 礼包不存在
			if (!moduleKits.contains(args[2])) {
				TLM.getInst().getLanguage().get("KIT-NOTFOUND").addPlaceholder("$kit", args[2]).send(sender);
				return;
			}
			
			// 获取玩家
			Player player;
			if (args.length > 3) {
				player = Bukkit.getPlayerExact(args[3]);
				// 玩家不存在
				if (player == null) {
					TLM.getInst().getLanguage().get("KIT-OFFLINE").addPlaceholder("$name", args[3]).send(sender);
					return;
				}
			} else if (sender instanceof Player) {
				player = (Player) sender;
			} else {
				TLM.getInst().getLanguage().get("KIT-CONSOLE").send(sender);
				return;
			}
			
			// 是否领取
			if (moduleKits.isPlayerRewared(player, args[2])) {
				// 是否只能领取一次
				if (moduleKits.isDisposable(args[2])) {
					TLM.getInst().getLanguage().get("KIT-DISPOSABLE").addPlaceholder("$kit", args[2]).send(sender);
					return;
				}
				// 是否冷却中
				if (moduleKits.isPlayerCooldown(player, args[2])) {
					TLM.getInst().getLanguage().get("KIT-COOLDOWN").addPlaceholder("$kit", args[2]).send(sender);
					return;
				}
			}
			
			// 是否有权限领取
			String permission = moduleKits.getPermission(args[2]);
			if (permission != null && !player.hasPermission(permission)) {
				// 提示信息
				player.sendMessage(moduleKits.getPermissionMessage(args[2]));
				return;
			}
			
			// 发送礼包
			List<ItemStack> items = moduleKits.getItems(args[2]);
			for (ItemStack item : items) {
				// 给予物品
				HashMap<Integer, ItemStack> result = player.getInventory().addItem(item);
				// 如果背包空间不足
				if (result.size() > 0 && moduleKits.isFullDrop(args[2])) {
					// 掉落物品
					player.getWorld().dropItem(player.getLocation(), item);
				}
			}
			
			// 执行命令
			for (String command : moduleKits.getCommands(args[2])) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("$player", player.getName()));
			}
			
			// 已领取
			moduleKits.setPlayerReward(player, args[2], true);
			
			// 提示信息
			TLM.getInst().getLanguage().get("KIT-SUCCESS").addPlaceholder("$kit", args[2]).send(sender);
		}
		else if (args[1].equalsIgnoreCase("reset")) {
			// 判断权限
			if (!sender.hasPermission("taboolib.kit.reset")) {
				TLM.getInst().getLanguage().get("NOPERMISSION-KIT-RESET").send(sender);
				return;
			}
			
			// 检查礼包
			if (args.length < 3) {
				TLM.getInst().getLanguage().get("KIT-NAME").send(sender);
				return;
			}
			
			// 礼包不存在
			if (!moduleKits.contains(args[2])) {
				TLM.getInst().getLanguage().get("KIT-NOTFOUND").addPlaceholder("$kit", args[2]).send(sender);
				return;
			}
			
			// 获取玩家
			Player player;
			if (args.length > 3) {
				player = Bukkit.getPlayerExact(args[3]);
				// 玩家不存在
				if (player == null) {
					TLM.getInst().getLanguage().get("KIT-OFFLINE").addPlaceholder("$name", args[3]).send(sender);
					return;
				}
				else {
					moduleKits.setPlayerReward(player, args[2], false);
					TLM.getInst().getLanguage().get("KIT-RESET-PLAYER").addPlaceholder("$kit", args[2]).addPlaceholder("$player", player.getName()).send(sender);
				}
			} else {
				moduleKits.resetKit(args[2]);
				TLM.getInst().getLanguage().get("KIT-RESET-ALL").addPlaceholder("$kit", args[2]).send(sender);
			}
		}
		else {
			TLM.getInst().getLanguage().get("COMMAND-ERROR").send(sender);
		}
	}

}
