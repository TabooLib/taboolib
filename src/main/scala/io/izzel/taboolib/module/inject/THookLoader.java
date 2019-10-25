package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @Author sky
 * @Since 2019-08-17 22:32
 */
public class THookLoader implements TabooLibLoader.Loader {

    @Override
    public void activeLoad(Plugin plugin, Class<?> pluginClass) {
        if (pluginClass.isAnnotationPresent(THook.class)) {
            // PlaceholderAPI
            if (TabooLibAPI.getPluginBridge().placeholderHooked() && TabooLibAPI.getPluginBridge().isPlaceholderExpansion(pluginClass)) {
                TabooLibAPI.getPluginBridge().registerExpansion(pluginClass);
            }
        }
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            if (Plugin.class.isAssignableFrom(declaredField.getType()) && declaredField.isAnnotationPresent(THook.class)) {
                THook hook = declaredField.getAnnotation(THook.class);
                if (Strings.nonEmpty(hook.plugin())) {
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
                    Ref.forcedAccess(declaredField);
                    try {
                        declaredField.set(instance, Bukkit.getPluginManager().getPlugin(hook.plugin()));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }
}
