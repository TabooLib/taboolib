package com.ilummc.tlib.inject;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.*;
import com.ilummc.tlib.dependency.TDependency;
import com.ilummc.tlib.util.TLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

public class DependencyInjector {

    public static void inject(Plugin plugin, Object o) {
    	try {
    		injectConfig(plugin, o);
    	} catch (NoClassDefFoundError ignored) {
    	}
    	try {
    		injectLogger(plugin, o);
    	} catch (NoClassDefFoundError ignored) {
    	}
    	try {
    		injectPluginInstance(plugin, o);
    	} catch (NoClassDefFoundError ignored) {
    	}
    	try {
    		injectDependencies(plugin, o);
    	} catch (NoClassDefFoundError ignored) {
    	}
    }

    static void injectOnEnable(Plugin plugin) {
        inject(plugin, plugin);
    }

    static void onDisable(Plugin plugin) {
    	try {
    		ejectConfig(plugin, plugin);
    	} catch (NoClassDefFoundError ignored) {
    	}
    }

    public static void eject(Plugin plugin, Object o) {
    	try {
    		ejectConfig(plugin, o);
    	} catch (NoClassDefFoundError ignored) {
    	}
    }

    private static void ejectConfig(Plugin plugin, Object o) {
        for (Field field : o.getClass().getDeclaredFields()) {
            Config config;
            if ((config = field.getType().getAnnotation(Config.class)) != null) {
                try {
                    field.setAccessible(true);
                    TConfigInjector.saveConfig(plugin, o);
                    TLib.getTLib().getLogger().info("插件 " + plugin + " 的配置 " + config.name() + " 已保存");
                } catch (Exception e) {
                    TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置 " + config.name() + " 保存失败");
                }
            }
        }
    }

    private static void injectConfig(Plugin plugin, Object o) {
        for (Field field : o.getClass().getDeclaredFields()) {
            try {
                Config config;
                if ((config = field.getType().getAnnotation(Config.class)) != null) {
                    field.setAccessible(true);
                    Object obj = TConfigInjector.loadConfig(plugin, field.getType());
                    if (obj != null) {
                        TLib.getTLib().getLogger().info("插件 " + plugin.getName() + " 的 " + config.name() + " 配置文件成功加载");
                        field.set(o, obj);
                        if (config.listenChanges()) {
                            TLib.getTLib().getLogger().info("开始监听插件 " + plugin.getName() + " 的 " + config.name() + " 配置文件");
                            TLib.getTLib().getConfigWatcher().addOnListen(
                                    new File(plugin.getDataFolder(), config.name()),
                                    obj,
                                    object -> {
                                        try {
                                            Object newObj = TConfigInjector.loadConfig(plugin, object.getClass());
                                            for (Field f : newObj.getClass().getDeclaredFields()) {
                                                f.setAccessible(true);
                                                f.set(obj, f.get(newObj));
                                            }
                                            TLib.getTLib().getLogger().info("插件 " + plugin.getName() + " 的 " + config.name() + " 配置文件成功重载");
                                        } catch (Exception ignored) {
                                            TLib.getTLib().getLogger().warn("插件 " + plugin.getName() + " 的 " + config.name() + " 配置文件重载时发生错误");
                                        }
                                    }
                            );
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void injectLogger(Plugin plugin, Object o) {
    	for (Field field : o.getClass().getDeclaredFields()) {
    		try {
    			Logger logger;
                if ((logger = field.getAnnotation(Logger.class)) != null) {
                    field.getType().asSubclass(TLogger.class);
                    TLogger tLogger = new TLogger(logger.value(), plugin, logger.level());
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    field.set(o, tLogger);
                }
        	} catch (Exception ignored2) {
        	}
		}
    }

    private static void injectPluginInstance(Plugin plugin, Object o) {
    	for (Field field : o.getClass().getDeclaredFields()) {
        	try {
        		PluginInstance instance;
                if ((instance = field.getAnnotation(PluginInstance.class)) != null) {
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    field.getType().asSubclass(JavaPlugin.class);
                    Plugin pl;
                    if ((pl = Bukkit.getPluginManager().getPlugin(instance.value())) == null) {
                        if (!TDependency.requestPlugin(instance.value())) {
                            TLib.getTLib().getLogger().warn(plugin.getName() + " 所需的依赖插件 " + instance.value() + " 自动加载失败");
                            return;
                        } else {
                            pl = Bukkit.getPluginManager().getPlugin(instance.value());
                        }
                    }
                    if (pl != null)
                        field.set(o, pl);
                }
        	} catch (Exception ignored) {
        	}
        }
    }

    private static void injectDependencies(Plugin plugin, Object o) {
        Dependency[] dependencies = new Dependency[0]; {
            Dependencies d = o.getClass().getAnnotation(Dependencies.class);
            if (d != null) {
            	dependencies = d.value();
            }
            Dependency d2 = o.getClass().getAnnotation(Dependency.class);
            if (d2 != null) {
            	dependencies = new Dependency[]{d2};
            }
        }
        if (dependencies.length != 0) {
            TLib.getTLib().getLogger().info("正在加载 " + plugin.getName() + " 插件所需的依赖");
            for (Dependency dependency : dependencies) {
                if (dependency.type() == Dependency.Type.PLUGIN) {
                    if (TDependency.requestPlugin(dependency.plugin())) {
                        TLib.getTLib().getLogger().info("  " + plugin.getName() + " 请求的插件 " + dependency.plugin() + " 加载成功。");
                    } else {
                        TLib.getTLib().getLogger().warn("  " + plugin.getName() + " 请求的插件 " + dependency.plugin() + " 加载失败。");
                    }
                }
                if (dependency.type() == Dependency.Type.LIBRARY) {
                    if (TDependency.requestLib(dependency.maven(), dependency.mavenRepo(), dependency.url())) {
                        TLib.getTLib().getLogger().info("  " + plugin.getName() + " 请求的库文件 " + String.join(":", dependency.maven()) + " 加载成功。");
                    } else {
                        TLib.getTLib().getLogger().warn("  " + plugin.getName() + " 请求的库文件 " + String.join(":", dependency.maven()) + " 加载失败。");
                    }
                }
            }
            TLib.getTLib().getLogger().info("依赖加载完成");
        }
    }

}
