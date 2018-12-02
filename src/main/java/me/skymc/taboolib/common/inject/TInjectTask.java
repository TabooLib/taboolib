package me.skymc.taboolib.common.inject;

import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

/**
 * @Author sky
 * @Since 2018-10-05 13:41
 */
public interface TInjectTask {

    void run(Plugin plugin, Field field, String[] args, Object instance);

}
