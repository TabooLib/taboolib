package io.izzel.taboolib.module.inject;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.command.lite.CommandBuilder;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.module.packet.TPacketHandler;
import io.izzel.taboolib.module.packet.TPacketListener;
import io.izzel.taboolib.util.lite.cooldown.Cooldown;
import io.izzel.taboolib.util.lite.cooldown.Cooldowns;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-10-05 13:40
 */
public class TInjectLoader implements TabooLibLoader.Loader {

    private static Map<Class<?>, TInjectTask> injectTypes = Maps.newLinkedHashMap();

    static {
        // Instance Inject
        injectTypes.put(Plugin.class, (plugin, field, args, pluginClass, instance) -> {
            try {
                field.set(instance, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TLogger Inject
        injectTypes.put(TLogger.class, (plugin, field, args, pluginClass, instance) -> {
            try {
                field.set(instance, args.value().length == 0 ? TLogger.getUnformatted(plugin) : TLogger.getUnformatted(args.value()[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TPacketListener Inject
        injectTypes.put(TPacketListener.class, (plugin, field, args, pluginClass, instance) -> {
            try {
                TPacketHandler.addListener(plugin, ((TPacketListener) field.get(instance)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TConfiguration Inject
        injectTypes.put(TConfig.class, (plugin, field, args, pluginClass, instance) -> {
            try {
                TConfig config = TConfig.create(plugin, args.value().length == 0 ? "config.yml" : args.value()[0]);
                if (!args.reload().isEmpty()) {
                    try {
                        Method declaredMethod = pluginClass.getDeclaredMethod(args.reload());
                        declaredMethod.setAccessible(true);
                        config.listener(() -> {
                            try {
                                declaredMethod.invoke(instance);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        });
                        TabooLibLoader.runTask(config::runListener);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                field.set(instance, config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // SimpleCommandBuilder Inject
        injectTypes.put(CommandBuilder.class, (plugin, field, args, pluginClass, instance) -> {
            try {
                CommandBuilder builder = (CommandBuilder) field.get(instance);
                if (!builder.isBuild()) {
                    if (builder.getPlugin() == null) {
                        builder.plugin(plugin);
                    }
                    builder.build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // CooldownPack Inject
        injectTypes.put(Cooldown.class, (plugin, field, args, pluginClass, instance) -> {
            try {
                Cooldowns.register((Cooldown) field.get(instance), plugin);
            } catch (Throwable t) {
                t.printStackTrace();
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
                    TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field. (" + pluginClass.getName() + ")");
                    continue;
                }
            }
            inject(plugin, declaredField, instance, annotation, injectTypes.get(Plugin.class), pluginClass);
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
                    TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field. (" + pluginClass.getName() + ")");
                    continue;
                }
            }
            TInjectTask tInjectTask = injectTypes.get(declaredField.getType());
            if (tInjectTask != null) {
                inject(plugin, declaredField, instance, annotation, tInjectTask, pluginClass);
            }
        }
    }

    public void inject(Plugin plugin, Field field, Object instance, TInject annotation, TInjectTask injectTask, Class pluginClass) {
        try {
            field.setAccessible(true);
            injectTask.run(plugin, field, annotation, pluginClass, instance);
            TabooLibAPI.debug(field.getName() + " injected. (" + field.getType().getName() + ")");
        } catch (Throwable e) {
            TLogger.getGlobalLogger().error(field.getName() + " inject failed: " + e.getMessage() + " (" + field.getName() + ")");
            if (e.getMessage() == null) {
                e.printStackTrace();
            }
        }
    }
}
