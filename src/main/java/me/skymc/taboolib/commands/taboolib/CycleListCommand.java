package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import org.bukkit.command.CommandSender;

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
