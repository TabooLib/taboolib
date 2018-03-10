package me.skymc.taboolib.commands.sub;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;

public class HelpCommand extends SubCommand {
	
	public HelpCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		HashMap<String, String> helps = new LinkedHashMap<>();
		helps.put("/taboolib save §8[§7名称§8]", "§e保存手中物品");
		helps.put("/taboolib item §8[§7名称§8] §8<§7玩家§8> §8<§7数量§8>", "§e给予玩家物品");
		helps.put("/taboolib iteminfo", "§e查看物品信息");
		helps.put("/taboolib itemlist", "§e查看所有物品");
		helps.put("/taboolib itemreload", "§e重载物品缓存");
		helps.put("§a", null);
		helps.put("/taboolib attributes", "§e查看所有属性");
		helps.put("/taboolib enchants", "§e查看所有附魔");
		helps.put("/taboolib potions", "§e查看所有药水");
		helps.put("/taboolib flags", "§e查看所有标签");
		helps.put("/taboolib slots", "§e查看所有部位");
		helps.put("§b", null);
		helps.put("/taboolib getvariable §8[§7-s|a§8] §8[§7键§8]", "§e查看变量");
		helps.put("/taboolib setvariable §8[§7-s|a§8] §8[§7键§8] §8[§7值§8]", "§e更改变量");
		helps.put("§c", null);
		helps.put("/taboolib cycle list", "§e列出所有时间检查器");
		helps.put("/taboolib cycle info §8[§7名称§8]", "§e查询检查器信息");
		helps.put("/taboolib cycle reset §8[§7名称§8]", "§e初始化时间检查器");
		helps.put("/taboolib cycle update §8[§7名称§8]", "§e更新时间检查器");
		helps.put("§f", null);
		helps.put("/taboolib shell load §8[§7名称§8]", "§e载入某个脚本");
		helps.put("/taboolib shell unload §8[§7名称§8]", "§e卸载某个脚本");
		helps.put("§e", null);
		helps.put("/taboolib importdata", "§4向数据库导入本地数据 §8(该操作将会清空数据库)");
		
		if (sender instanceof ConsoleCommandSender || TabooLib.getVerint() < 10900) {
			sender.sendMessage("§f");
			sender.sendMessage("§b§l----- §3§lTaooLib Commands §b§l-----");
			sender.sendMessage("§f");
			// 遍历命令
			for (Entry<String, String> entry : helps.entrySet()) {
				if (entry.getValue() == null) {
					sender.sendMessage("§f");
				} else {
					sender.sendMessage("§f " + entry.getKey() + " §6- " + entry.getValue());
				}
			}
			sender.sendMessage("§f");
		}
		else if (sender instanceof Player) {
			JSONFormatter json = new JSONFormatter();
			json.append("§f"); json.newLine();
			json.append("§b§l----- §3§lTaooLib Commands §b§l-----"); json.newLine();
			json.append("§f"); json.newLine();
			// 遍历命令
			for (Entry<String, String> entry : helps.entrySet()) {
				if (entry.getValue() == null) {
					json.append("§f"); json.newLine();
				} else {
					json.appendHoverClick("§f " + entry.getKey() + " §6- " + entry.getValue(), new ShowTextEvent("§f点击复制指令"), new SuggestCommandEvent(entry.getKey().split("§")[0])); json.newLine();
				}
			}
			json.append("§f");
			json.send((Player) sender);
		}
	}
}
