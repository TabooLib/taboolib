package io.izzel.taboolib.module.db.local;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Ref;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @Author 坏黑
 * @Since 2019-07-06 17:35
 */
public class LocalLoader implements TabooLibLoader.Loader {

    @Override
    public void preLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field field : pluginClass.getDeclaredFields()) {
            LocalFile annotation = field.getAnnotation(LocalFile.class);
            if (annotation == null) {
                continue;
            }
            Object instance = null;
            // 如果是非静态类型
            if (!Modifier.isStatic(field.getModifiers())) {
                // 是否为主类
                if (pluginClass.equals(plugin.getClass())) {
                    instance = plugin;
                } else {
                    TLogger.getGlobalLogger().error(field.getName() + " is not a static field. (" + pluginClass.getName() + ")");
                    continue;
                }
            }
            Ref.forcedAccess(field);
            try {
                field.set(instance, Local.get(plugin.getName()).get(annotation.value()));
            } catch (IllegalAccessException ignored) {
            }
        }
    }
}
