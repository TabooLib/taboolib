package com.ilummc.tlib.inject;

import com.ilummc.tlib.annotations.Dependencies;
import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.annotations.Logger;
import com.ilummc.tlib.annotations.PluginInstance;
import com.ilummc.tlib.dependency.TDependency;
import com.ilummc.tlib.util.TLogger;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class DependencyInjector {

    public static void inject(Plugin plugin, Object o) {
        injectLogger(plugin, o);
        injectPluginInstance(plugin, o);
        injectDependencies(plugin, o);
    }

    static void injectOnEnable(Plugin plugin) {
        inject(plugin, plugin);
    }

    static void onDisable(Plugin plugin) {

    }

    private static void injectLogger(Plugin plugin, Object o) {
        try {
            for (Field field : o.getClass().getDeclaredFields()) {
                Logger logger;
                if ((logger = field.getAnnotation(Logger.class)) != null) {
                    field.getType().asSubclass(TLogger.class);
                    TLogger tLogger = new TLogger(logger.value(), plugin, logger.level());
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    field.set(o, tLogger);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void injectPluginInstance(Plugin plugin, Object o) {
        try {
            for (Field field : o.getClass().getDeclaredFields()) {
                PluginInstance instance;
                if ((instance = field.getAnnotation(PluginInstance.class)) != null) {
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    field.getType().asSubclass(JavaPlugin.class);
                    Plugin pl;
                    if ((pl = Bukkit.getPluginManager().getPlugin(instance.value())) == null) {
                        if (!TDependency.requestPlugin(instance.value())) {
                            MsgUtils.warn(plugin.getName() + " 所需的依赖插件 " + instance.value() + " 自动加载失败");
                            return;
                        } else {
                            pl = Bukkit.getPluginManager().getPlugin(instance.value());
                        }
                    }
                    if (pl != null)
                        field.set(o, pl);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void injectDependencies(Plugin plugin, Object o) {
        Dependency[] dependencies = new Dependency[0];
        {
            Dependencies d = o.getClass().getAnnotation(Dependencies.class);
            if (d != null) dependencies = d.value();
            Dependency d2 = o.getClass().getAnnotation(Dependency.class);
            if (d2 != null) dependencies = new Dependency[]{d2};
        }
        if (dependencies.length != 0) {
            MsgUtils.send("正在加载 " + plugin.getName() + " 插件所需的依赖");
            for (Dependency dependency : dependencies) {
                if (dependency.type() == Dependency.Type.PLUGIN)
                    if (TDependency.requestPlugin(dependency.args()))
                        MsgUtils.send(plugin.getName() + " 请求的插件 " + dependency.args()[0] + " 加载成功。");
                    else
                        MsgUtils.warn(plugin.getName() + " 请求的插件 " + dependency.args()[0] + " 加载失败。");
                if (dependency.type() == Dependency.Type.LIBRARY)
                    if (TDependency.requestLib(dependency.args()))
                        MsgUtils.send(plugin.getName() + " 请求的库文件 " + String.join(":", dependency.args()) + " 加载成功。");
                    else
                        MsgUtils.send(plugin.getName() + " 请求的库文件 " + String.join(":", dependency.args()) + " 加载失败。");
            }
            MsgUtils.send("依赖加载完成");
        }
    }

}
