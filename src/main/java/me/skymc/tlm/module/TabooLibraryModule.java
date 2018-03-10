package me.skymc.tlm.module;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import me.skymc.taboolib.Main;
import me.skymc.tlm.annotation.DisableConfig;

/**
 * @author sky
 * @since 2018年2月17日 下午11:22:48
 */
public class TabooLibraryModule {
	
	private final HashMap<ITabooLibraryModule, FileConfiguration> TLM_MODULE = new HashMap<>();
	private static TabooLibraryModule inst = null;
	
	private TabooLibraryModule() {
		
	}
	
	public static TabooLibraryModule getInst() {
		if (inst == null) {
			synchronized (TabooLibraryModule.class) {
				if (inst == null) {
					inst = new TabooLibraryModule();
				}
			}
		}
		return inst;
	}
	
	public void register(ITabooLibraryModule module) {
		if (!TLM_MODULE.containsKey(module)) {
			TLM_MODULE.put(module, new YamlConfiguration());
			reloadConfig(module, false);
		}
	}
	
	public void loadModules() {
		for (ITabooLibraryModule module : TLM_MODULE.keySet()) {
			module.onEnable();
			if (module instanceof Listener) {
				Bukkit.getPluginManager().registerEvents((Listener) module, Main.getInst());
			}
		}
	}
	
	public void unloadModules() {
		TLM_MODULE.keySet().forEach(x -> x.onDisable());
	}
	
	public void reloadConfig() {
		TLM_MODULE.keySet().forEach(x -> reloadConfig(x, true));
	}
	
	public void reloadConfig(ITabooLibraryModule module, boolean isReload) {
		if (module.getName() == null || module.getClass().getAnnotation(DisableConfig.class) != null) {
			return;
		}
		File file = new File(Main.getInst().getDataFolder(), "TLM/" + module.getName() + ".yml");
		if (!file.exists()) {
			Main.getInst().saveResource("TLM/" + module.getName() + ".yml", true);
		}
		try {
			TLM_MODULE.get(module).load(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		} 
		if (isReload) {
			module.onReload();
		}
	}
	
	public FileConfiguration getConfig(ITabooLibraryModule module) {
		return TLM_MODULE.get(module);
	}
	
	public int getSize() {
		return TLM_MODULE.size();
	}
	
	public Set<ITabooLibraryModule> keySet() {
		return TLM_MODULE.keySet();
	}
	
	public ITabooLibraryModule valueOf(String name) {
		for (ITabooLibraryModule module : TLM_MODULE.keySet()) {
			if (module.getName().equals(name)) {
				return module;
			}
		}
		return null;
	}
}
