package io.izzel.taboolib.module.inject;

import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public interface TValueTask {

    void run(Plugin plugin, Field field, TValue inject, Class<?> pluginClass, Object instance);

}
