package io.izzel.taboolib.module.inject;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibLoader;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 坏黑
 * @since 2018-12-15 15:09
 */
public class TScheduleLoader implements TabooLibLoader.Loader {

    static Map<String, List<TScheduleData>> schedules = Maps.newHashMap();

    public static void run(Plugin plugin) {
        List<TScheduleData> dataList = schedules.remove(plugin.getName());
        if (dataList != null) {
            dataList.forEach(data -> run(plugin, data.getRunnable(), data.getAnnotation().delay(), data.getAnnotation().period(), data.getAnnotation().async()));
        }
    }

    public static void run(Plugin plugin, BukkitRunnable runnable, int delay, int period, boolean async) {
        if (async) {
            runnable.runTaskTimerAsynchronously(plugin, delay, period);
        } else {
            runnable.runTaskTimer(plugin, delay, period);
        }
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        for (Method method : pluginClass.getDeclaredMethods()) {
            TSchedule annotation = method.getAnnotation(TSchedule.class);
            if (annotation == null) {
                continue;
            }
            method.setAccessible(true);
            Object instance = TInjectHelper.getInstance(method, pluginClass, plugin).get(0);
            if (plugin.equals(TabooLib.getPlugin())) {
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
            } else {
                List<TScheduleData> dataList = schedules.computeIfAbsent(plugin.getName(), n -> new ArrayList<>());
                dataList.add(new TScheduleData(annotation, new BukkitRunnable() {

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
