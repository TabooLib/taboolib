package me.skymc.taboolib.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;

public class PotionCommand extends SubCommand {

	@SuppressWarnings("deprecation")
	public PotionCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		sender.sendMessage("§f");
		sender.sendMessage("§b§l----- §3§lPotionEffect Types §b§l-----");
		sender.sendMessage("§f");
		
		for (PotionEffectType type : PotionEffectType.values()) {
			if (type != null) {
				if (isPlayer()) {
					JSONFormatter json = new JSONFormatter();
					json.append(" §7- §f" + type.getId() + ". " + type.getName());
					json.appendHoverClick(" §8(点击复制)", new ShowTextEvent("§f点击复制"), new SuggestCommandEvent(type.getName()));
					json.send((Player) sender);
				}
				else {
					sender.sendMessage(" §7- §f" + type.getId() + ". " + type.getName() + "");
				}
			}
		}
		sender.sendMessage("§f");
	}
}
