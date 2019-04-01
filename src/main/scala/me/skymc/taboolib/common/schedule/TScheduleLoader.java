package me.skymc.taboolib.common.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ilummc.tlib.logger.TLogger;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.TabooLibLoader;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2018-12-15 15:09
 */
public class TScheduleLoader implements Listener, TabooLibLoader.Loader {

    private Map<String, List<TScheduleData>> schedules = Maps.newHashMap();

    TScheduleLoader() {
        Bukkit.getPluginManager().registerEvents(this, TabooLib.instance());
    }

    public static void run(Plugin plugin, BukkitRunnable runnable, int delay, int period, boolean async) {
        if (async) {
            runnable.runTaskTimerAsynchronously(plugin, delay, period);
        } else {
            runnable.runTaskTimer(plugin, delay, period);
        }
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        Optional.ofNullable(schedules.remove(e.getPlugin().getName())).ifPresent(list -> list.forEach(scheduleData -> {
            run(e.getPlugin(), scheduleData.getRunnable(), scheduleData.getAnnotation().delay(), scheduleData.getAnnotation().period(), scheduleData.getAnnotation().async());
        }));
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> loadClass) {
        for (Method method : loadClass.getDeclaredMethods()) {
            TSchedule annotation = method.getAnnotation(TSchedule.class);
            if (annotation == null) {
                continue;
            }
            Object instance = loadClass.equals(plugin.getClass()) ? plugin : null;
            // 如果是非静态类型
            if (!Modifier.isStatic(method.getModifiers()) && instance == null) {
                // 是否为主类
                TLogger.getGlobalLogger().error(method.getName() + " is not a static method.");
                continue;
            }
            method.setAccessible(true);
            //  如果是本插件
            if (plugin.equals(TabooLib.instance())) {
                run(plugin, new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            method.invoke(instance);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }, annotation.delay(), annotation.period(), annotation.async());
            }
            // 其他插件则添加到列队
            else {
                schedules.computeIfAbsent(plugin.getName(), n -> Lists.newArrayList()).add(new TScheduleData(annotation, new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            method.invoke(instance);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }));
            }
        }
    }
}
