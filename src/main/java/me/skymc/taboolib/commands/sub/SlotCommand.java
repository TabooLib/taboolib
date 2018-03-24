package me.skymc.taboolib.commands.sub;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SlotCommand extends SubCommand {

	public SlotCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		sender.sendMessage("§f");
		sender.sendMessage("§b§l----- §3§lAttribute Slots §b§l-----");
		sender.sendMessage("§f");
		
		String[] attributes = new String[] { "mainhand", "offhand", "feet", "legs", "chest", "head", "all" };
		
		for (String name : attributes) {
			if (isPlayer()) {
				JSONFormatter json = new JSONFormatter();
				json.append(" §7- §f" + name);
				json.appendHoverClick(" §8(点击复制)", new ShowTextEvent("§f点击复制"), new SuggestCommandEvent(name));
				json.send((Player) sender);
			}
			else {
				sender.sendMessage(" §7- §f" + name);
			}
		}
		sender.sendMessage("§f");
	}
}
