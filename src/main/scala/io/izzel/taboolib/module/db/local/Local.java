package io.izzel.taboolib.module.db.local;

import com.google.common.collect.Maps;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.util.Ref;

import java.util.Map;
import java.util.Optional;

/**
 * 本地数据工具
 *
 * @Author 坏黑
 * @Since 2019-07-06 15:24
 */
public class Local {

    private static final Map<String, LocalPlugin> plugins = Maps.newConcurrentMap();

    @TSchedule(delay = 20 * 30, period = 20 * 30, async = true)
    public static void saveFiles() {
        plugins.values().forEach(LocalPlugin::saveFiles);
    }

    public static void saveFiles(String name) {
        Optional.ofNullable(plugins.get(name)).ifPresent(LocalPlugin::saveFiles);
    }

    public static void clearFiles(String name) {
        Optional.ofNullable(plugins.remove(name)).ifPresent(LocalPlugin::clearFiles);
    }

    public static LocalPlugin get(String name) {
        return plugins.computeIfAbsent(name, LocalPlugin::new);
    }

    public static LocalPlugin get() {
        Class<?> callerClass = Ref.getCallerClass(3).orElse(null);
        return get(callerClass == null ? "TabooLib" : Ref.getCallerPlugin(callerClass).getName());
    }
}
