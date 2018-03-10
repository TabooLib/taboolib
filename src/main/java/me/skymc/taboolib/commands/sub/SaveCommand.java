package me.skymc.taboolib.commands.sub;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.ChatCatcher;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.message.ChatCatcher.Catcher;
import me.skymc.taboolib.playerdata.DataUtils;

public class SaveCommand extends SubCommand {

	public SaveCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (!(sender instanceof Player)) {
			MsgUtils.send(sender, "&4后台无法这么做");
			return;
		}
		
		if (args.length < 2) {
			MsgUtils.send(sender, "&4请输入正确的名称");
			return;
		}
		
		if (((Player) sender).getItemInHand().getType().equals(Material.AIR)) {
			MsgUtils.send(sender, "&4你不能保存空气");
			return;
		}
		
		if (ItemUtils.getItemCachesFinal().containsKey(args[1])) {
			MsgUtils.send(sender, "&4该名称所对应的物品保存于固定物品库中, 无法覆盖");
			return;
		}
		
		if (ItemUtils.getItemCaches().containsKey(args[1])) {
			// 检查聊天引导
			if (ChatCatcher.contains((Player) sender)) {
				MsgUtils.send(sender, "&4你有一个正在进行的聊天引导, 请完成后在这么做");
				return;
			}
			
			ChatCatcher.call((Player) sender, new ChatCatcher.Catcher() {
				
				@Override
				public void cancel() {
					MsgUtils.send(sender, "&7退出引导");
				}
				
				@Override
				public Catcher before() {
					MsgUtils.send(sender, "物品 &f" + args[1] + "&7 已存在, 如果你想要覆盖它, 请在聊天框中输入 \"&f是&7\"");
					return this;
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public boolean after(String message) {
					if (message.equals("是")) {
						saveItem(args[1], ((Player) sender).getItemInHand());
						MsgUtils.send(sender, "物品 &f" + args[1] + " &7已替换");
					}
					else {
						MsgUtils.send(sender, "&7退出引导");
					}
					return false;
				}
			});
		}
		else {
			saveItem(args[1], ((Player) sender).getItemInHand());
			MsgUtils.send(sender, "物品 &f" + args[1] + " &7已保存");
		}
	}

	
	private void saveItem(String name, ItemStack item) {
		FileConfiguration conf = ConfigUtils.load(Main.getInst(), ItemUtils.getItemCacheFile());
		conf.set(name + ".bukkit", item);
		DataUtils.saveConfiguration(conf, ItemUtils.getItemCacheFile());
		ItemUtils.reloadItemCache();
	}
}
