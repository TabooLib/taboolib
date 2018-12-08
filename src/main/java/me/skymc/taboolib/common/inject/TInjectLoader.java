package me.skymc.taboolib.common.inject;

import com.google.common.collect.Maps;
import com.ilummc.tlib.logger.TLogger;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.TabooLibLoader;
import me.skymc.taboolib.commands.builder.SimpleCommandBuilder;
import me.skymc.taboolib.common.configuration.TConfiguration;
import me.skymc.taboolib.common.packet.TPacketHandler;
import me.skymc.taboolib.common.packet.TPacketListener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-10-05 13:40
 */
public class TInjectLoader implements TabooLibLoader.Loader {

    private static Map<Class<?>, TInjectTask> injectTypes = Maps.newHashMap();

    static {
        // Instance Inject
        injectTypes.put(Plugin.class, (plugin, field, args, instance) -> {
            try {
                field.set(instance, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TLogger Inject
        injectTypes.put(TLogger.class, (plugin, field, args, instance) -> {
            try {
                field.set(instance, args.length == 0 ? TLogger.getUnformatted(plugin) : TLogger.getUnformatted(args[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TPacketListener Inject
        injectTypes.put(TPacketListener.class, (plugin, field, args, instance) -> {
            try {
                TPacketHandler.addListener(plugin, ((TPacketListener) field.get(instance)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TConfiguration Inject
        injectTypes.put(TConfiguration.class, (plugin, field, args, instance) -> {
            try {
                if (args.length == 0) {
                    TLogger.getGlobalLogger().error("Invalid inject arguments: " + field.getName() + " (" + field.getType().getName() + ")");
                } else {
                    field.set(instance, TConfiguration.createInResource(plugin, args[0]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // SimpleCommandBuilder Inject
        injectTypes.put(SimpleCommandBuilder.class, (plugin, field, args, instance) -> {
            try {
                SimpleCommandBuilder builder = (SimpleCommandBuilder) field.get(instance);
                if (builder.isBuild()) {
                    TLogger.getGlobalLogger().error("Command was registered.  (" + field.getType().getName() + ")");
                } else {
                    if (builder.getPlugin() == null) {
                        builder.plugin(plugin);
                    }
                    builder.build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int priority() {
        return -999;
    }

    @Override
    public void preLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            // 是否为主类类型
            if (annotation == null || !declaredField.getType().equals(plugin.getClass())) {
                continue;
            }
            Object instance = null;
            // 如果是非静态类型
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                // 是否为主类
                if (pluginClass.equals(plugin.getClass())) {
                    instance = plugin;
                } else {
                    TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field. (" + declaredField.getType().getName() + ")");
                    continue;
                }
            }
            inject(plugin, declaredField, instance, annotation, injectTypes.get(Plugin.class));
        }
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            if (annotation == null || declaredField.getType().equals(plugin.getClass())) {
                continue;
            }
            Object instance = null;
            // 如果是非静态类型
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                // 是否为主类
                if (pluginClass.equals(plugin.getClass())) {
                    instance = plugin;
                } else {
                    TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field. (" + declaredField.getType().getName() + ")");
                    continue;
                }
            }
            TInjectTask tInjectTask = injectTypes.get(declaredField.getType());
            if (tInjectTask != null) {
                inject(plugin, declaredField, instance, annotation, tInjectTask);
            } else {
                TLogger.getGlobalLogger().error(declaredField.getName() + " is an invalid inject type. (" + declaredField.getType().getName() + ")");
            }
        }
    }

    public void inject(Plugin plugin, Field field, Object instance, TInject annotation, TInjectTask injectTask) {
        try {
            field.setAccessible(true);
            injectTask.run(plugin, field, annotation.value(), instance);
            TabooLib.debug(field.getName() + " injected. (" + field.getType().getName() + ")");
        } catch (Throwable e) {
            TLogger.getGlobalLogger().error(field.getName() + " inject failed: " + e.getMessage() + " (" + field.getType().getName() + ")");
            if (e.getMessage() == null) {
                e.printStackTrace();
            }
        }
    }
}
