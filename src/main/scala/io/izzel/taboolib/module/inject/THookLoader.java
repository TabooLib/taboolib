package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.compat.PlaceholderHook;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

/**
 * @author sky
 * @since 2019-08-17 22:32
 */
public class THookLoader implements TabooLibLoader.Loader {

    @Override
    public void activeLoad(Plugin plugin, Class<?> pluginClass) {
        if (pluginClass.isAnnotationPresent(THook.class)) {
            if (TabooLibAPI.getPluginBridge().placeholderHooked()) {
                // PlaceholderAPI
                if (TabooLibAPI.getPluginBridge().isPlaceholderExpansion(pluginClass)) {
                    TabooLibAPI.getPluginBridge().registerExpansion(pluginClass);
                }
                // PlaceholderHook Expansion
                if (PlaceholderHook.Expansion.class.isAssignableFrom(pluginClass)) {
                    TabooLibAPI.getPluginBridge().registerExpansionProxy(pluginClass);
                }
            }
        }
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            if (Plugin.class.isAssignableFrom(declaredField.getType()) && declaredField.isAnnotationPresent(THook.class)) {
                THook hook = declaredField.getAnnotation(THook.class);
                if (Strings.nonEmpty(hook.plugin())) {
                    for (Object instance : TInjectHelper.getInstance(declaredField, pluginClass, plugin)) {
                        try {
                            Ref.putField(instance, declaredField, Bukkit.getPluginManager().getPlugin(hook.plugin()));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
