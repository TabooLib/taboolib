package me.skymc.tlm.module.sub;

import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.other.DateUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.timecycle.TimeCycle;
import me.skymc.taboolib.timecycle.TimeCycleEvent;
import me.skymc.taboolib.timecycle.TimeCycleInitializeEvent;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import me.skymc.tlm.module.ITabooLibraryModule;

/**
 * @author sky
 * @since 2018年2月17日 下午11:23:38
 */
public class ModuleTimeCycle implements ITabooLibraryModule, Listener {
	
	@Override
	public String getName() {
		return "TimeCycle";
	}
	
	@Override
	public void onEnable() {
		// 载入检查器
		loadCycles();
	}
	
	@Override
	public void onDisable() {
		// 注销检查器
		unloadCycles();
	}
	
	@Override
	public void onReload() {
		// 注销检查器
		unloadCycles();
		// 载入检查器
		loadCycles();
	}
	
	@EventHandler
	public void onTimeCycleInitialize(TimeCycleInitializeEvent e) {
		if (e.getCycle().getName().contains("tlm|")) {
			// 获取名称
			String name = e.getCycle().getName().replace("tlm|", "");
			// 如果有初始化时间配置
			if (getConfig().contains("TimeCycle." + name + ".Initialise.InitialiseDate")) {
				// 获取时间
				Calendar date = Calendar.getInstance();
				// 遍历初始化规则
				for (String typeStr : getConfig().getStringList("TimeCycle." + name + ".Initialise.InitialiseDate")) {
					try {
						int type = (int) Calendar.class.getField(typeStr.split("=")[0]).get(Calendar.class);
						date.set(type, NumberUtils.getInteger(typeStr.split("=")[1]));
					} catch (Exception err) {
						MsgUtils.warn("模块配置载入异常: &4日期类型错误");
						MsgUtils.warn("模块: &4TimeCycle");
						MsgUtils.warn("位于: &4" + typeStr);
					}
				}
				e.setTimeLine(date.getTimeInMillis());
			}
			// 如果有初始化命令
			if (getConfig().contains("TimeCycle." + name + ".Initialise.InitialiseCommand")) {
				// 遍历初始化命令
				for (String command : getConfig().getStringList("TimeCycle." + name + ".Initialise.InitialiseCommand")) {
					// 执行命令
					Bukkit.getScheduler().runTask(Main.getInst(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
				}
			}
		}
	}
	
	@EventHandler
	public void onTimeCycle(TimeCycleEvent e) {
		if (e.getCycle().getName().contains("tlm|")) {
			// 获取名称
			String name = e.getCycle().getName().replace("tlm|", "");
			// 如果有更新命令
			if (getConfig().contains("TimeCycle." + name + ".UpdateCommand")) {
				// 遍历更新命令
				for (String command : getConfig().getStringList("TimeCycle." + name + ".UpdateCommand")) {
					// 执行命令
					Bukkit.getScheduler().runTask(Main.getInst(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
				}
			}
		}
	}
	
	private void loadCycles() {
		for (String name : getConfig().getConfigurationSection("TimeCycle").getKeys(false)) {
			TimeCycleManager.register(new TimeCycle("tlm|" + name, DateUtils.formatDate(getConfig().getString("TimeCycle." + name + ".Cycle")), Main.getInst()));
		}
	}
	
	private void unloadCycles() {
		for (TimeCycle cycle : TimeCycleManager.getTimeCycles()) {
			if (cycle.getName().startsWith("tlm|")) {
				TimeCycleManager.cancel(cycle.getName());
			}
		}
	}
}
