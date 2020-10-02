package io.izzel.taboolib.module.inject;

import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

/**
 * @Author sky
 * @Since 2018-10-05 13:41
 */
public interface TInjectTask {

    void run(Plugin plugin, Field field, TInject inject, Class<?> pluginClass, Object instance);

}
