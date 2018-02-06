package me.skymc.taboolib.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.commands.sub.AttributesCommand;
import me.skymc.taboolib.commands.sub.EnchantCommand;
import me.skymc.taboolib.commands.sub.FlagCommand;
import me.skymc.taboolib.commands.sub.ImportCommand;
import me.skymc.taboolib.commands.sub.InfoCommand;
import me.skymc.taboolib.commands.sub.ItemCommand;
import me.skymc.taboolib.commands.sub.PotionCommand;
import me.skymc.taboolib.commands.sub.SaveCommand;
import me.skymc.taboolib.commands.sub.SlotCommand;
import me.skymc.taboolib.commands.sub.VariableGetCommand;
import me.skymc.taboolib.commands.sub.VariableSetCommand;
import me.skymc.taboolib.commands.sub.itemlist.ItemListCommand;
import me.skymc.taboolib.commands.sub.shell.ShellCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.MsgUtils;

public class MainCommands implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("§f");
			sender.sendMessage("§b§l----- §3§lTaooLib Commands §b§l-----");
			sender.sendMessage("§f");
			sender.sendMessage("§7 /taboolib save [名称] §f- §8保存手中物品");
			sender.sendMessage("§7 /taboolib item/i [物品] <玩家> <数量> §f- §8给予玩家物品");
			sender.sendMessage("§7 /taboolib iteminfo §f- §8查看物品信息");
			sender.sendMessage("§7 /taboolib itemlist §f- §8查看所有物品");
			sender.sendMessage("§7 /taboolib itemreload/ireload §f- §8重载物品缓存");
			sender.sendMessage("§f");
			sender.sendMessage("§7 /taboolib attributes §f- §8查看所有属性");
			sender.sendMessage("§7 /taboolib enchants §f- §8查看所有附魔");
			sender.sendMessage("§7 /taboolib potions §f- §8查看所有药水");
			sender.sendMessage("§7 /taboolib flags §f- §8查看所有标签");
			sender.sendMessage("§7 /taboolib slots §f- §8查看所有部位");
			sender.sendMessage("§f");
			sender.sendMessage("§7 /taboolib getvariable [-s|a] [键] §f- §8查看变量");
			sender.sendMessage("§7 /taboolib setvariable [-s|a] [键] [值] §f- §8更改变量");
			sender.sendMessage("§f");
			sender.sendMessage("§7 /taboolib shell/s load [脚本] §f- §8载入某个脚本");
			sender.sendMessage("§7 /taboolib shell/s unload [脚本] §f- §8卸载某个脚本");
			sender.sendMessage("§f");
			sender.sendMessage("§c /taboolib importdata §f- §4向数据库导入本地数据 §8(该操作将会清空数据库)");
			sender.sendMessage("§f");
			return false;
		}
		else if (args[0].equalsIgnoreCase("itemreload") || args[0].equalsIgnoreCase("ireload")) {
			ItemUtils.reloadItemCache();
			ItemUtils.reloadItemName();
			MsgUtils.send(sender, "重载成功");
			return true;
		}
		else if (args[0].equalsIgnoreCase("save")) {
			return new SaveCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("enchants")) {
			return new EnchantCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("potions")) {
			return new PotionCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("flags")) {
			return new FlagCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("attributes")) {
			return new AttributesCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("slots")) {
			return new SlotCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("importdata")) {
			return new ImportCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("iteminfo")) {
			return new InfoCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("itemlist")) {
			return new ItemListCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("item") || args[0].equalsIgnoreCase("i")) {
			return new ItemCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("setvariable")) {
			return new VariableSetCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("getvariable")) {
			return new VariableGetCommand(sender, args).command();
		}
		else if (args[0].equalsIgnoreCase("shell") || args[0].equalsIgnoreCase("s")) {
			return new ShellCommand(sender, args).command();
		}
		else {
			MsgUtils.send(sender, "&4指令错误");
		}
		return false;
	}
}
