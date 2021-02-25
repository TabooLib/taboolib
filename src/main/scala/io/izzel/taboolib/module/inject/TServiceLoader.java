package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

/**
 * @author sky
 * @since 2018-09-08 14:00
 */
public class TServiceLoader implements TabooLibLoader.Loader {

    @SuppressWarnings("rawtypes")
    @Override
    public void preLoad(Plugin plugin, Class pluginClass) {
        if (pluginClass.isAnnotationPresent(TService.class)) {
            TService service = (TService) pluginClass.getAnnotation(TService.class);
            try {
                Bukkit.getServicesManager().register(pluginClass, TInjectHelper.getInstance(pluginClass, plugin).get(0), plugin, ServicePriority.Normal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        if (pluginClass.isAnnotationPresent(TService.class)) {
            TService service = pluginClass.getAnnotation(TService.class);
            try {
                Bukkit.getServicesManager().unregister(pluginClass, Bukkit.getServicesManager().load(pluginClass));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
