package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.other.DateUtils;
import me.skymc.taboolib.timecycle.TimeCycle;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;

public class CycleInfoCommand extends SubCommand {

	public CycleInfoCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 2) {
			TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
			return;
		}
		
		TimeCycle cycle = TimeCycleManager.getTimeCycle(args[2]);
		if (cycle == null) {
			TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.INVALID-CYCLE", args[2]);
			return;
		}

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.CYCLE-INFO",
                asString(cycle.getCycle() / 1000L),
                cycle.getPlugin().getName(),
                DateUtils.CH_ALL.format(TimeCycleManager.getBeforeTimeline(cycle.getName())),
                DateUtils.CH_ALL.format(TimeCycleManager.getAfterTimeline(cycle.getName())));
	}
	
	public String asString(long seconds) {
		long day = TimeUnit.SECONDS.toDays(seconds);
	    long hours = TimeUnit.SECONDS.toHours(seconds) - day * 24;
	    long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60L;
	    long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60L;
	    return "§f" + day + "§7 天, §f" + hours + "§7 小时, §f" + minute + "§7 分钟, §f" + second + "§7 秒";
	}

	@Override
	public boolean command() {
		return true;
	}
}
