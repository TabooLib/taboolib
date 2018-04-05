package com.ilummc.tlib;

import com.ilummc.tlib.annotations.Config;
import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.annotations.Logger;
import com.ilummc.tlib.inject.DependencyInjector;
import com.ilummc.tlib.inject.TLibPluginManager;
import com.ilummc.tlib.util.TLogger;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Field;

@Dependency(type = Dependency.Type.LIBRARY, maven = "org.ow2.asm:asm:6.1.1")
@Dependency(type = Dependency.Type.LIBRARY, maven = "com.zaxxer:HikariCP:3.0.0")
public class TLib {

    private static TLib tLib;

    @Logger("§3[§6TLib§3|{1}§3] §f{2}")
    private TLogger tLogger;

    private TLibConfig config;

    private TLib() {
    }

    public TLibConfig getConfig() {
        return config;
    }

    public TLogger getLogger() {
        return tLogger;
    }

    public static TLib getTLib() {
        return tLib;
    }

    @SuppressWarnings({"unchecked"})
    public static void init() {
        new File(Main.getInst().getDataFolder(), "/libs").mkdirs();
        tLib = new TLib();
        DependencyInjector.inject(Main.getInst(), tLib);
        // 注入 PluginLoader 用于加载依赖
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("pluginManager");
            field.setAccessible(true);
            field.set(Bukkit.getServer(), new TLibPluginManager());
            tLib.getLogger().info("注入成功");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            tLib.getLogger().fatal("注入失败");
        }
    }

    @Config(name = "tlib.yml")
    public class TLibConfig {

        private int downloadPoolSize = 4;

        public int getDownloadPoolSize() {
            return downloadPoolSize;
        }
    }

}
