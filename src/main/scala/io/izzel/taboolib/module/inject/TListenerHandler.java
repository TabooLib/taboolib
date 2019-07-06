package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.cronus.util.StringExpression;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @Author sky
 * @Since 2018-08-22 13:48
 */
public class TListenerHandler {

    private static HashMap<String, List<Listener>> listeners = new HashMap<>();

    /**
     * 初始化所有插件的所有监听器
     */
    public static void setupListeners() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            try {
                setupListener(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化插件的所有监听器
     * 该操作会执行无参构造方法
     *
     * @param plugin 插件
     */
    public static void setupListener(Plugin plugin) {
        for (Class<?> pluginClass : TabooLibLoader.getPluginClassSafely(plugin)) {
            if (Listener.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(TListener.class)) {
                try {
                    TListener tListener = pluginClass.getAnnotation(TListener.class);
                    // 检查版本
                    if (!new StringExpression(tListener.version()).isSelect(Version.getCurrentVersion().getVersionInt())) {
                        continue;
                    }
                    // 检查注册条件
                    if (tListener.depend().length > 0 && !Strings.isBlank(tListener.depend()[0])) {
                        if (Arrays.stream(tListener.depend()).anyMatch(depend -> Bukkit.getPluginManager().getPlugin(depend) == null)) {
                            continue;
                        }
                    }
                    // 实例化监听器
                    Listener listener = plugin.getClass().equals(pluginClass) ? (Listener) plugin : (Listener) Reflection.instantiateObject(pluginClass);
                    try {
                        listeners.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(listener);
                        TabooLibAPI.debug("Listener " + listener.getClass().getSimpleName() + " setup successfully. (" + plugin.getName() + ")");
                    } catch (Exception e) {
                        TLogger.getGlobalLogger().warn("TListener setup Failed: " + pluginClass.getName());
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    TabooLibAPI.debug("Listener " + pluginClass.getSimpleName() + "(" + plugin.getName() + ")" + " setup failed: " + e.toString());
                }
            }
        }
    }

    /**
     * 注册所有插件的所有监听器
     */
    public static void registerListeners() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            try {
                registerListener(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 注册插件的所有监听器
     * 该操作会执行 TListener 注解中的 register() 对应方法
     *
     * @param plugin 插件
     */
    public static void registerListener(Plugin plugin) {
        Optional.ofNullable(listeners.get(plugin.getName())).ifPresent(listeners -> {
            for (Listener listener : listeners) {
                TListener tListener = listener.getClass().getAnnotation(TListener.class);
                // 检查注册条件
                if (!Strings.isBlank(tListener.condition())) {
                    try {
                        Method method = listener.getClass().getDeclaredMethod(tListener.condition());
                        method.setAccessible(true);
                        if (!(boolean) method.invoke(listener)) {
                            continue;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // 执行注册方法
                if (!Strings.isBlank(tListener.register())) {
                    try {
                        Method method = listener.getClass().getDeclaredMethod(tListener.register());
                        method.setAccessible(true);
                        method.invoke(listener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // 注册监听
                Bukkit.getPluginManager().registerEvents(listener, plugin);
                TabooLibAPI.debug("Listener " + listener.getClass().getSimpleName() + " registered. (" + plugin.getName() + ")");
            }
        });
    }

    /**
     * 注销所有插件的所有监听器
     */
    public static void cancelListeners() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            try {
                cancelListener(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 注销插件的所有监听器
     * 该操作会执行 TListener 注解中的 cancel() 对应方法
     *
     * @param plugin 插件
     */
    public static void cancelListener(Plugin plugin) {
        Optional.ofNullable(listeners.remove(plugin.getName())).ifPresent(listeners -> {
            for (Listener listener : listeners) {
                HandlerList.unregisterAll(listener);
                TListener tListener = listener.getClass().getAnnotation(TListener.class);
                if (!Strings.isBlank(tListener.cancel())) {
                    try {
                        Method method = listener.getClass().getDeclaredMethod(tListener.cancel());
                        method.setAccessible(true);
                        method.invoke(listener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static HashMap<String, List<Listener>> getListeners() {
        return listeners;
    }
}
