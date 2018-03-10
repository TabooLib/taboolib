package me.skymc.tlm;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.string.language2.Language2;
import me.skymc.tlm.module.TabooLibraryModule;
import me.skymc.tlm.module.sub.ModuleCommandChanger;
import me.skymc.tlm.module.sub.ModuleInventorySave;
import me.skymc.tlm.module.sub.ModuleKits;
import me.skymc.tlm.module.sub.ModuleTimeCycle;

/**
 * @author sky
 * @since 2018年2月17日 下午10:28:05
 */
public class TLM {
	
	private static TLM inst = null;
	
	@Getter
	private FileConfiguration config;
	
	@Getter
	private Language2 language;
	
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
		if (isEnableModule("Kits")) {
			TabooLibraryModule.getInst().register(new ModuleKits());
		}
		if (isEnableModule("CommandChanger")) {
			TabooLibraryModule.getInst().register(new ModuleCommandChanger());
		}
		if (isEnableModule("InventorySave")) {
			TabooLibraryModule.getInst().register(new ModuleInventorySave());
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
		// 载入语言文件
		try {
			language = new Language2(config.getString("Language"), Main.getInst());
		} catch (Exception e) {
			MsgUtils.warn("语言文件不存在: &4" + config.getString("Language"));
			return;
		}
	}
	
	/**
	 * 模块是否启用
	 * 
	 * @param name 名称
	 * @return boolean
	 */
	public boolean isEnableModule(String name) {
		return config.getStringList("EnableModule").contains(name);
	}
}
