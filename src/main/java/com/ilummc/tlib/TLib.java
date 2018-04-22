package com.ilummc.tlib;

import com.ilummc.tlib.annotations.Config;
import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.annotations.Logger;
import com.ilummc.tlib.compat.PlaceholderApiHook;
import com.ilummc.tlib.inject.DependencyInjector;
import com.ilummc.tlib.inject.TConfigWatcher;
import com.ilummc.tlib.inject.TLibPluginManager;
import com.ilummc.tlib.resources.LocaleLoader;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.TLogger;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Field;

@Dependency(type = Dependency.Type.LIBRARY, maven = "org.ow2.asm:asm:6.1.1")
@Dependency(type = Dependency.Type.LIBRARY, maven = "com.zaxxer:HikariCP:3.1.0")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.slf4j:slf4j-api:1.7.25")
public class TLib {

    private static TLib tLib;

    @Logger("§8[§3§lTabooLib§8][§r{1}§8] §f{2}")
    private TLogger tLogger = new TLogger("§8[§3§lTabooLib§8][§r{1}§8] §f{2}", Main.getInst(), TLogger.FINE);

    private TLibConfig config;

    private TConfigWatcher configWatcher = new TConfigWatcher();

    private TLib() {
    }

    public TLibConfig getConfig() {
        return config;
    }

    public TLogger getLogger() {
        return tLogger;
    }

    public TConfigWatcher getConfigWatcher() {
        return configWatcher;
    }

    public static TLib getTLib() {
        return tLib;
    }

    public static void injectPluginManager() {
        // 注入 PluginLoader 用于加载依赖
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("pluginManager");
            field.setAccessible(true);
            field.set(Bukkit.getServer(), new TLibPluginManager());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        new File(Main.getInst().getDataFolder(), "/libs").mkdirs();
        tLib = new TLib();
        LocaleLoader.init();
        PlaceholderApiHook.init();
        DependencyInjector.inject(Main.getInst(), tLib);
        if (Bukkit.getPluginManager() instanceof TLibPluginManager)
            tLib.getLogger().info("注入成功");
        else
            tLib.getLogger().fatal("注入失败");
        TLocale.sendToConsole("test1");
        TLocale.sendToConsole("test2");
        TLocale.sendToConsole("test3");
        TLocale.sendToConsole("test4.node1", "Hello", "world");
    }

    public static void unload() {
        tLib.getConfigWatcher().unregisterAll();
        DependencyInjector.eject(Main.getInst(), tLib);
    }

    @Config(name = "tlib.yml", listenChanges = true, readOnly = false)
    public static class TLibConfig {

        private int downloadPoolSize = 4;

        public int getDownloadPoolSize() {
            return downloadPoolSize;
        }

        private String[] locale = {"zh_CN", "en_US"};

        public String[] getLocale() {
            return locale;
        }

        private boolean enablePapiByDefault = false;

        public boolean isEnablePapiByDefault() {
            return enablePapiByDefault;
        }
    }
}
