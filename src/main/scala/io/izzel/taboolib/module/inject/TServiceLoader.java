package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

/**
 * @Author sky
 * @Since 2018-09-08 14:00
 */
public class TServiceLoader implements TabooLibLoader.Loader {

    @Override
    public void preLoad(Plugin plugin, Class pluginClass) {
        if (pluginClass.isAnnotationPresent(TService.class)) {
            TService service = (TService) pluginClass.getAnnotation(TService.class);
            try {
                Bukkit.getServicesManager().register(pluginClass, service.value().newInstance(), plugin, ServicePriority.Normal);
                TabooLibAPI.debug("Service " + pluginClass.getSimpleName() + " registered. (" + plugin.getName() + ")");
            } catch (Exception e) {
                TLogger.getGlobalLogger().warn("TService load Failed: " + pluginClass.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unload(Plugin plugin, Class pluginClass) {
        if (pluginClass.isAnnotationPresent(TService.class)) {
            TService service = (TService) pluginClass.getAnnotation(TService.class);
            try {
                Bukkit.getServicesManager().unregister(pluginClass, Bukkit.getServicesManager().load(pluginClass));
                TabooLibAPI.debug("Service " + pluginClass.getSimpleName() + " unregistered. (" + plugin.getName() + ")");
            } catch (Exception e) {
                TLogger.getGlobalLogger().warn("TService unload Failed: " + pluginClass.getName());
                e.printStackTrace();
            }
        }
    }
}
