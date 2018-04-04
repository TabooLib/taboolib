package com.ilummc.tlib;

import com.ilummc.tlib.inject.TLibPluginManager;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Field;

public class TLib {

    @SuppressWarnings({"unchecked"})
    public static void init() {
        // 注入 PluginLoader 用于加载依赖
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("pluginManager");
            field.setAccessible(true);
            field.set(Bukkit.getServer(), new TLibPluginManager());
            MsgUtils.send("注入成功");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            MsgUtils.warn("注入失败");
        }
        new File(Main.getInst().getDataFolder(), "/libs").mkdirs();
    }

}
