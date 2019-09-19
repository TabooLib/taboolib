package io.izzel.taboolib.module.inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.compat.kotlin.CompatKotlin;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
            Object[] instance = new Object[] {pluginClass.equals(plugin.getClass()) ? plugin : null};
            if (CompatKotlin.isCompanion(pluginClass)) {
                instance[0] = CompatKotlin.getCompanion(pluginClass);
                if (instance[0] == null) {
                    TLogger.getGlobalLogger().error(method.getName() + " required @JvmStatic.");
                    return;
                }
            } else if (!Modifier.isStatic(method.getModifiers()) && instance[0] == null) {
                TLogger.getGlobalLogger().error(method.getName() + " is not a static method.");
                continue;
            }
            if (plugin.equals(TabooLib.getPlugin())) {
                run(plugin, new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            method.setAccessible(true);
                            method.invoke(instance[0]);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }, annotation.delay(), annotation.period(), annotation.async());
            } else {
                schedules.computeIfAbsent(plugin.getName(), n -> Lists.newArrayList()).add(new TScheduleData(annotation, new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            method.setAccessible(true);
                            method.invoke(instance[0]);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }));
            }
        }
    }
}
