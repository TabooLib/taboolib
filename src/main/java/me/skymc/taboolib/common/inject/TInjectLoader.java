package me.skymc.taboolib.common.inject;

import com.google.common.collect.Maps;
import com.ilummc.tlib.logger.TLogger;
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
        injectTypes.put(Plugin.class, (plugin, field, args) -> {
            try {
                field.set(null, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TLogger Inject
        injectTypes.put(TLogger.class, (plugin, field, args) -> {
            try {
                field.set(null, args.length == 0 ? TLogger.getUnformatted(plugin) : TLogger.getUnformatted(args[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TPacketListener Inject
        injectTypes.put(TPacketListener.class, (plugin, field, args) -> {
            try {
                TPacketHandler.addListener(plugin, ((TPacketListener) field.get(null)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TConfiguration Inject
        injectTypes.put(TConfiguration.class, (plugin, field, args) -> {
            try {
                if (args.length == 0) {
                    TLogger.getGlobalLogger().error("Invalid inject arguments: " + field.getName() + " (" + field.getType().getName() + ")");
                } else {
                    field.set(null, TConfiguration.createInResource(plugin, args[0]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // SimpleCommandBuilder Inject
        injectTypes.put(SimpleCommandBuilder.class, (plugin, field, args) -> {
            try {
                SimpleCommandBuilder builder = (SimpleCommandBuilder) field.get(null);
                if (builder.isBuild()) {
                    TLogger.getGlobalLogger().error("Command was registered.  (" + field.getType().getName() + ")");
                } else {
                    builder.build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void preLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            if (annotation == null) {
                continue;
            }
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field. (" + declaredField.getType().getName() + ")");
                continue;
            }
            if (declaredField.getType().equals(plugin.getClass())) {
                try {
                    declaredField.setAccessible(true);
                    injectTypes.get(Plugin.class).run(plugin, declaredField, annotation.value());
                } catch (Exception e) {
                    TLogger.getGlobalLogger().error(declaredField.getName() + " inject failed: " + e.getMessage() + " (" + declaredField.getType().getName() + ")");
                }
            }
        }
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            if (annotation == null || declaredField.getType().equals(plugin.getClass())) {
                continue;
            }
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field. (" + declaredField.getType().getName() + ")");
                continue;
            }
            TInjectTask tInjectTask = injectTypes.get(declaredField.getType());
            if (tInjectTask == null) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " is an invalid inject type. (" + declaredField.getType().getName() + ")");
                continue;
            }
            try {
                declaredField.setAccessible(true);
                tInjectTask.run(plugin, declaredField, annotation.value());
            } catch (Exception e) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " inject failed: " + e.getMessage() + " (" + declaredField.getType().getName() + ")");
            }
        }
    }
}
