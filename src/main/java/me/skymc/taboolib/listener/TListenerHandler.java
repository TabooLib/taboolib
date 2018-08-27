package me.skymc.taboolib.listener;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @Author sky
 * @Since 2018-08-22 13:48
 */
@TListener
public class TListenerHandler implements Listener {

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
        if (!(plugin.equals(TabooLib.instance()) || TabooLib.isDependTabooLib(plugin))) {
            return;
        }
        List<Class> classes = FileUtils.getClasses(plugin);
        for (Class<?> pluginClass : classes) {
            if (Listener.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(TListener.class)) {
                try {
                    TListener tListener = pluginClass.getAnnotation(TListener.class);
                    // 检查注册条件
                    if (tListener.depend().length > 0 && !Strings.isBlank(tListener.depend()[0])) {
                        if (Arrays.stream(tListener.depend()).anyMatch(depend -> Bukkit.getPluginManager().getPlugin(depend) == null)) {
                            continue;
                        }
                    }
                    // 实例化监听器
                    Listener listener = plugin.getClass().equals(pluginClass) ? (Listener) plugin : (Listener) pluginClass.newInstance();
                    listeners.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(listener);
                } catch (Exception e) {
                    e.printStackTrace();
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

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        try {
            setupListener(e.getPlugin());
            registerListener(e.getPlugin());
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        try {
            cancelListener(e.getPlugin());
        } catch (Exception ignored) {
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static HashMap<String, List<Listener>> getListeners() {
        return listeners;
    }
}
