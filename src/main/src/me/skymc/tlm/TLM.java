package me.skymc.tlm;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.tlm.module.TabooLibraryModule;
import me.skymc.tlm.module.sub.ModuleTimeCycle;

/**
 * @author sky
 * @since 2018年2月17日 下午10:28:05
 */
public class TLM {
	
	private static TLM inst = null;
	
	@Getter
	private FileConfiguration config;
	
	/**
	 * 构造方法
	 */
	private TLM() {
		// 重载配置文件
		reloadConfig();
		// 载入模块
		if (isEnableModule("TimeCycle")) {
			TabooLibraryModule.getInst().register(new ModuleTimeCycle());
		}
		// 载入模块
		TabooLibraryModule.getInst().loadModules();
		// 提示
		MsgUtils.send("载入 &f" + TabooLibraryModule.getInst().getSize() + " &7个 &fTLM &7模块");
	}
	
	/**
	 * 获取 TLM 对象
	 * 
	 * @return TLM
	 */
	public static TLM getInst() {
		if (inst == null) {
			synchronized (TLM.class) {
				if (inst == null) {
					inst = new TLM();
				}
			}
		}
		return inst;
	}
	
	/**
	 * 载入配置文件
	 */
	public void reloadConfig() {
		config = ConfigUtils.saveDefaultConfig(Main.getInst(), "module.yml");
	}
	
	/**
	 * 模块是否启用
	 * 
	 * @param name 名称
	 * @return boolean
	 */
	private boolean isEnableModule(String name) {
		return config.getStringList("EnableModule").contains(name);
	}
}
