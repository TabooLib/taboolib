package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.lite.SimpleVersionControl;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @Author sky
 * @Since 2019-08-18 0:47
 */
public class TInjectAsm implements TabooLibLoader.Loader {

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            if (annotation == null || annotation.asm().isEmpty()) {
                continue;
            }
            Object instance = null;
            // 如果是非静态类型
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                // 是否为主类
                if (pluginClass.equals(plugin.getClass())) {
                    instance = plugin;
                } else {
                    TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field. (" + pluginClass.getName() + ")");
                    continue;
                }
            }
            try {
                declaredField.setAccessible(true);
                declaredField.set(instance, SimpleVersionControl.createNMS(annotation.asm()).useCache().translate(plugin).newInstance());
            } catch (Throwable t) {
                TLogger.getGlobalLogger().warn("Cannot translate class \"" + declaredField.getType().getName() + "\": " + t.getMessage());
            }
        }
    }
}
