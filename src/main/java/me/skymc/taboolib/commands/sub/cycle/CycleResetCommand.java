package me.skymc.taboolib.commands.sub.cycle;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.timecycle.TimeCycle;
import me.skymc.taboolib.timecycle.TimeCycleEvent;
import me.skymc.taboolib.timecycle.TimeCycleInitializeEvent;
import me.skymc.taboolib.timecycle.TimeCycleManager;

public class CycleResetCommand extends SubCommand {

	public CycleResetCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 3) {
			MsgUtils.send(sender, "&c请输入正确的检查器名称");
			return;
		}
		
		TimeCycle cycle = TimeCycleManager.getTimeCycle(args[2]);
		if (cycle == null) {
			MsgUtils.send(sender, "&c检查器 &4" + args[2] + " &c不存在");
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
				MsgUtils.send(sender, "检查器 &f" + args[2] + " &7初始化完成");
			}
		}.runTaskAsynchronously(Main.getInst());
	}

	@Override
	public boolean command() {
		return true;
	}

}
