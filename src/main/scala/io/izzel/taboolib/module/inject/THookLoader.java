package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.TabooLibLoader;
import org.bukkit.plugin.Plugin;

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
    }
}
