package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

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
                    Ref.forcedAccess(declaredField);
                    for (Object instance : TInjectHelper.getInstance(declaredField, pluginClass, plugin)) {
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
}
