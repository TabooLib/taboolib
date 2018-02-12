package me.skymc.taboolib.timecycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.playerdata.DataUtils;

public class TimeCycleManager {
	
	/**
	 * 最后一次更新： 2018年1月16日21:07:49
	 * 
	 * @author sky
	 */

	private static ConcurrentHashMap<String, TimeCycle> cycles = new ConcurrentHashMap<>();
	
	/**
	 * 获取周期管理器
	 * 
	 * @param name
	 * @return
	 */
	public static TimeCycle getTimeCycle(String name) {
		return cycles.get(name);
	}
	
	/**
	 * 获取所有周期管理器
	 * 
	 * @return
	 */
	public static Collection<TimeCycle> getTimeCycles() {
		return cycles.values();
	}
	
	/**
	 * 彻底删除周期数据
	 * 
	 * @param name
	 */
	public static void deleteCycleData(String name) {
		HashMap<String, String> map = GlobalDataManager.getVariables();
		for (String _name : map.keySet()) {
			if (_name.startsWith("timecycle")) {
				GlobalDataManager.setVariable(name, null);
			}
		}
	}
	
	/**
	 * 注册周期管理器
	 * 
	 * @param cycle
	 */
	public static void register(TimeCycle cycle) {
		if (!cycles.containsKey(cycle.getName())) {
			cycles.put(cycle.getName(), cycle);
		}
		else {
			MsgUtils.warn("注册周期管理器 §8" + cycle.getName() + "§c 失败, 原因: &4名称重复");
		}
	}
	
	/**
	 * 注销周期管理器
	 * 
	 * @param name
	 * @return
	 */
	public static TimeCycle cancel(String name) {
		return cycles.remove(name);
	}
	
	/**
	 * 注销插件所有周期管理器
	 * 
	 * @param plugin
	 */
	public static void cancel(Plugin plugin) {
		cycles.values().forEach(x -> {
			if (x.getPlugin().equals(plugin)) {
				cycles.remove(x.getName());
			}
		});
	}
	
	/**
	 * 设置上一次更新事件
	 * 
	 * @param name
	 * @param time
	 */
	public static boolean setTimeline(String name, Long time) {
		if (cycles.containsKey(name)) {
			GlobalDataManager.setVariable("timecycle:" + name, time.toString());
			return true;
		}
		return false;
	}
	
	/**
	 * 获取下一次刷新时间
	 * 
	 * @param name
	 * @return
	 */
	public static long getAfterTimeline(String name) {
		if (cycles.containsKey(name)) {
			Long value = Long.valueOf(GlobalDataManager.getVariable("timecycle:" + name, "0"));
			return value + cycles.get(name).getCycle();
		}
		return 0L;
	}
	
	/**
	 * 获取上一次刷新时间
	 * 
	 * @param name
	 * @return
	 */
	public static long getBeforeTimeline(String name) {
		if (cycles.containsKey(name)) {
			return Long.valueOf(GlobalDataManager.getVariable("timecycle:" + name, "0"));
		}
		return 0L;
	}
	
	public static void load() {
		// 注册调度器
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for (TimeCycle cycle : cycles.values()) {
					// 调度器没有被执行过
					if (!GlobalDataManager.contains("timecycle:" + cycle.getName())) {
						long time = new TimeCycleInitializeEvent(cycle, System.currentTimeMillis()).call().getTimeline();
						// 初始化
						GlobalDataManager.setVariable("timecycle:" + cycle.getName(), String.valueOf(time));
						// 触发器
						Bukkit.getPluginManager().callEvent(new TimeCycleEvent(cycle));
					}
					// 如果超出刷新时间
					else if (System.currentTimeMillis() >= getAfterTimeline(cycle.getName())) {
						long time = System.currentTimeMillis();
						// 如果时间差大于 30 秒
						if (time - getAfterTimeline(cycle.getName()) > 30000) {
							// 初始化
							time = new TimeCycleInitializeEvent(cycle, time).call().getTimeline();
						}
						// 重置
						GlobalDataManager.setVariable("timecycle:" + cycle.getName(), String.valueOf(time));
						// 触发器
						Bukkit.getPluginManager().callEvent(new TimeCycleEvent(cycle));
					}
				}
			}
		}.runTaskTimerAsynchronously(Main.getInst(), 0, 20);
	}
}
