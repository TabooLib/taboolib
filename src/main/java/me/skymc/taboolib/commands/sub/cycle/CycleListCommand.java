package me.skymc.taboolib.commands.sub.cycle;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import me.skymc.taboolib.timecycle.TimeCycle;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CycleListCommand extends SubCommand {

	public CycleListCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		sender.sendMessage("§f");
		sender.sendMessage("§b§l----- §3§lTimeCycle List §b§l-----");
		sender.sendMessage("§f");
		
		for (TimeCycle cycle : TimeCycleManager.getTimeCycles()) {
			if (isPlayer()) {
				JSONFormatter json = new JSONFormatter();
				json.append(" §7- §f" + cycle.getName());
				json.appendHoverClick(" §8(点击复制)", new ShowTextEvent("§f点击复制"), new SuggestCommandEvent(cycle.getName()));
				json.send((Player) sender);
			}
			else {
				sender.sendMessage(" §7- §f" + cycle.getName());
			}
		}
		
		sender.sendMessage("§f");
	}
	
	@Override
	public boolean command() {
		return true;
	}

}
