package me.skymc.taboolib.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;

public class EnchantCommand extends SubCommand {

	@SuppressWarnings("deprecation")
	public EnchantCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		sender.sendMessage("§f");
		sender.sendMessage("§b§l----- §3§lItemStack Enchantments §b§l-----");
		sender.sendMessage("§f");
		
		for (Enchantment enchant : Enchantment.values()) {
			if (isPlayer()) {
				JSONFormatter json = new JSONFormatter();
				json.append(" §7- §f" + enchant.getId() + ". " + enchant.getName());
				json.appendHoverClick(" §8(点击复制)", new ShowTextEvent("§f点击复制"), new SuggestCommandEvent(enchant.getName()));
				json.send((Player) sender);
			}
			else {
				sender.sendMessage(" §7- §f" + enchant.getId() + ". " + enchant.getName());
			}
		}
		sender.sendMessage("§f");
	}
}
