package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.timecycle.TimeCycle;
import me.skymc.taboolib.timecycle.TimeCycleEvent;
import me.skymc.taboolib.timecycle.TimeCycleInitializeEvent;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CycleResetCommand extends SubCommand {

	public CycleResetCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 3) {
			TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
			return;
		}
		
		TimeCycle cycle = TimeCycleManager.getTimeCycle(args[2]);
		if (cycle == null) {
			TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.INVALID-CYCLE", args[2]);
			return;
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				long time = new TimeCycleInitializeEvent(cycle, System.currentTimeMillis()).call().getTimeline();
				// 初始化
				GlobalDataManager.setVariable("timecycle:" + cycle.getName(), String.valueOf(time));
				// 触发器
				Bukkit.getPluginManager().callEvent(new TimeCycleEvent(cycle));
				// 提示
				TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.CYCLE-RESET", args[2]);
			}
		}.runTaskAsynchronously(Main.getInst());
	}

	@Override
	public boolean command() {
		return true;
	}

}
