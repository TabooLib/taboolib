package io.izzel.taboolib.module.db.local;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.inject.TInjectHelper;
import io.izzel.taboolib.util.Ref;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

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
            Ref.forcedAccess(field);
            for (Object instance : TInjectHelper.getInstance(field, pluginClass, plugin)) {
                try {
                    field.set(instance, Local.get(plugin.getName()).get(annotation.value()));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
