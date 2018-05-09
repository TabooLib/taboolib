package me.skymc.taboolib.commands.sub.cycle;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import me.skymc.taboolib.timecycle.TimeCycle;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class CycleListCommand extends SubCommand {

	public CycleListCommand(CommandSender sender, String[] args) {
		super(sender, args);

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.LIST.HEAD");

        TimeCycleManager.getTimeCycles().forEach(cycle -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.LIST.BODY", cycle.getName()));

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.LIST.FOOT");
	}
	
	@Override
	public boolean command() {
		return true;
	}

}
