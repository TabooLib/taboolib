package io.izzel.taboolib.module.inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.util.Ref;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2018-12-15 15:09
 */
public class TScheduleLoader implements TabooLibLoader.Loader {

    static Map<String, List<TScheduleData>> schedules = Maps.newHashMap();

    public static void run(Plugin plugin) {
        List<TScheduleData> list = schedules.remove(plugin.getName());
        if (list != null) {
            list.forEach(data -> run(plugin, data.getRunnable(), data.getAnnotation().delay(), data.getAnnotation().period(), data.getAnnotation().async()));
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
            if (plugin.equals(TabooLib.getPlugin())) {
                method.setAccessible(true);
                TInjectHelper.getInstance(method, pluginClass, plugin).forEach(instance -> run(plugin, new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            method.invoke(instance);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }, annotation.delay(), annotation.period(), annotation.async()));
            } else {
                method.setAccessible(true);
                TInjectHelper.getInstance(method, pluginClass, plugin).forEach(instance -> schedules.computeIfAbsent(plugin.getName(), n -> Lists.newArrayList()).add(new TScheduleData(annotation, new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            method.invoke(instance);
                        } catch (Throwable t) {
                            try {
                                method.invoke(Ref.UNSAFE.allocateInstance(pluginClass));
                            } catch (Throwable t2) {
                                t.printStackTrace();
                                t2.printStackTrace();
                            }
                        }
                    }
                })));
            }
        }
    }
}
