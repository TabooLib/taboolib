package com.ilummc.tlib.inject;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.*;
import com.ilummc.tlib.dependency.TDependency;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.resources.TLocaleLoader;
import com.ilummc.tlib.util.Ref;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Izzel_Aliz
 */
public class TDependencyInjector {

    private static List<String> injected = new ArrayList<>();

    public static Dependency[] getDependencies(Object o) {
        Dependency[] dependencies = new Dependency[0];
        Dependencies d = o.getClass().getAnnotation(Dependencies.class);
        if (d != null) {
            dependencies = d.value();
        }
        Dependency d2 = o.getClass().getAnnotation(Dependency.class);
        if (d2 != null) {
            dependencies = new Dependency[]{d2};
        }
        return dependencies;
    }

    static void injectOnEnable(Plugin plugin) {
        inject(plugin, plugin);
    }

    static void ejectOnDisable(Plugin plugin) {
        eject(plugin, plugin);
    }

    public static void inject(Plugin plugin, Object o) {
        if (!injected.contains(plugin.getName())) {
            injected.add(plugin.getName());
            TLocaleLoader.load(plugin, true);
            injectDependencies(plugin, o);
            injectLogger(plugin, o);
            injectConfig(plugin, o);
            injectPluginInstance(plugin, o);
        }
    }

    public static void eject(Plugin plugin, Object o) {
        try {
            injected.remove(plugin.getName());
            ejectConfig(plugin, o);
        } catch (Throwable ignored) {
        }
    }

    private static void ejectConfig(Plugin plugin, Object o) {
        for (Field field : Ref.getDeclaredFields(o.getClass())) {
            TConfig config;
            if ((config = field.getType().getAnnotation(TConfig.class)) != null && config.saveOnExit()) {
                try {
                    field.setAccessible(true);
                    TConfigInjector.saveConfig(plugin, field.get(o));
                    TLocale.Logger.info("CONFIG.SAVE-SUCCESS", plugin.toString(), config.name());
                } catch (Exception e) {
                    TLocale.Logger.warn("CONFIG.SAVE-FAIL", plugin.toString(), config.name());
                    e.printStackTrace();
                }
            }
        }
    }

    private static void injectConfig(Plugin plugin, Object o) {
        for (Field field : Ref.getDeclaredFields(o.getClass())) {
            try {
                TConfig config;
                if ((config = field.getType().getAnnotation(TConfig.class)) != null) {
                    field.setAccessible(true);
                    Object obj = TConfigInjector.loadConfig(plugin, field.getType());
                    if (obj != null) {
                        TLocale.Logger.info("CONFIG.LOAD-SUCCESS", plugin.toString(), config.name());
                        field.set(o, obj);
                        if (config.listenChanges()) {
                            TLocale.Logger.info("CONFIG.LISTEN-START", plugin.toString(), config.name());
                            TLib.getTLib().getConfigWatcher().addOnListen(
                                    new File(plugin.getDataFolder(), config.name()),
                                    obj,
                                    object -> {
                                        try {
                                            TConfigInjector.reloadConfig(plugin, object);
                                            TLocale.Logger.info("CONFIG.RELOAD-SUCCESS", plugin.toString(), config.name());
                                        } catch (Exception ignored) {
                                            TLocale.Logger.warn("CONFIG.RELOAD-FAIL", plugin.toString(), config.name());
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
        for (Field field : Ref.getDeclaredFields(o.getClass())) {
            try {
                Logger logger;
                if ((logger = field.getAnnotation(Logger.class)) != null) {
                    field.getType().asSubclass(com.ilummc.tlib.logger.TLogger.class);
                    com.ilummc.tlib.logger.TLogger tLogger = new com.ilummc.tlib.logger.TLogger(logger.value(), plugin, logger.level());
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.set(o, tLogger);
                    TLoggerManager.setDefaultLogger(plugin, tLogger);
                }
            } catch (Exception ignored2) {
            }
        }
    }

    private static void injectPluginInstance(Plugin plugin, Object o) {
        for (Field field : Ref.getDeclaredFields(o.getClass())) {
            try {
                PluginInstance instance;
                if ((instance = field.getAnnotation(PluginInstance.class)) != null) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.getType().asSubclass(JavaPlugin.class);
                    Plugin pl;
                    if ((pl = Bukkit.getPluginManager().getPlugin(instance.value())) == null) {
                        if (!TDependency.requestPlugin(instance.value())) {
                            TLocale.Logger.warn("PLUGIN-AUTOLOAD-FAIL", plugin.getName(), instance.value());
                            return;
                        } else {
                            pl = Bukkit.getPluginManager().getPlugin(instance.value());
                        }
                    }
                    if (pl != null) {
                        field.set(o, pl);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void injectDependencies(Plugin plugin, Object o) {
        Dependency[] dependencies = getDependencies(o);
        if (dependencies.length != 0) {
            TLocale.Logger.info("DEPENDENCY.LOADING-START", plugin.getName());
            for (Dependency dependency : dependencies) {
                if (dependency.type() == Dependency.Type.PLUGIN) {
                    if (TDependency.requestPlugin(dependency.plugin())) {
                        TLocale.Logger.info("DEPENDENCY.PLUGIN-LOAD-SUCCESS", plugin.getName(), dependency.plugin());
                    } else {
                        TLocale.Logger.warn("DEPENDENCY.PLUGIN-LOAD-FAIL", plugin.getName(), dependency.plugin());
                    }
                }
                if (dependency.type() == Dependency.Type.LIBRARY) {
                    if (TDependency.requestLib(dependency.maven(), dependency.mavenRepo(), dependency.url())) {
                        TLocale.Logger.info("DEPENDENCY.LIBRARY-LOAD-SUCCESS", plugin.getName(), String.join(":", dependency.maven()));
                    } else {
                        TLocale.Logger.warn("DEPENDENCY.LIBRARY-LOAD-FAIL", plugin.getName(), String.join(":", dependency.maven()));
                    }
                }
            }
            TLocale.Logger.info("DEPENDENCY.LOAD-COMPLETE");
        }
    }
}
