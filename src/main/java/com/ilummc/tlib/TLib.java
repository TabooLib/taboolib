package com.ilummc.tlib;

import java.io.File;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;

import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.config.TLibConfig;
import com.ilummc.tlib.filter.TLoggerFilter;
import com.ilummc.tlib.inject.TConfigWatcher;
import com.ilummc.tlib.inject.TDependencyInjector;
import com.ilummc.tlib.inject.TPluginManager;
import com.ilummc.tlib.resources.TLocaleLoader;
import com.ilummc.tlib.util.TLogger;

import lombok.Getter;
import me.skymc.taboolib.Main;

@Dependency(type = Dependency.Type.LIBRARY, maven = "org.ow2.asm:asm:6.1.1")
@Dependency(type = Dependency.Type.LIBRARY, maven = "com.zaxxer:HikariCP:3.1.0")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.slf4j:slf4j-api:1.7.25")
public class TLib {

	@Getter
    private static TLib tLib;

	@Getter
    private TLogger logger = new TLogger("§8[§3§lTabooLib§8][§r{1}§8] §f{2}", Main.getInst(), TLogger.FINE);

    @Getter
    private TLibConfig config;

    @Getter
    private TConfigWatcher configWatcher = new TConfigWatcher();
    
    @Getter
    private File libsFolder;

    private TLib() {
    	libsFolder =  new File(Main.getInst().getDataFolder(), "/libs");
    	if (!libsFolder.exists()) {
    		libsFolder.mkdirs();
    	}
    }

    public static void init() {
        tLib = new TLib();
        
        TLoggerFilter.init();
        TLocaleLoader.init();
        PlaceholderHook.init();
        TDependencyInjector.inject(Main.getInst(), tLib);
        
        if (Bukkit.getPluginManager() instanceof TPluginManager) {
            tLib.getLogger().info("注入成功");
        } else {
            tLib.getLogger().fatal("注入失败");
        }
    }

    public static void unload() {
        tLib.getConfigWatcher().unregisterAll();
        TDependencyInjector.eject(Main.getInst(), tLib);
    }
    
    public static void injectPluginManager() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("pluginManager");
            field.setAccessible(true);
            field.set(Bukkit.getServer(), new TPluginManager());
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }
}
