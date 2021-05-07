package io.izzel.taboolib.module.inject;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Strings;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author ElaBosak233
 */
public class TValueLoader implements TabooLibLoader.Loader{

    private static final Map<Class<?>, TValueTask> injectTypes = Maps.newLinkedHashMap();

    static {
        // Configuration Contents Inject
        injectTypes.put(Object.class, ((plugin, field, args, pluginClass, instance) -> {
            if (Strings.nonEmpty(args.node())) {
                TConfig config = TConfig.create(plugin, args.value().length == 0 ? "config.yml" : args.value()[0]);
                Ref.putField(instance, field, config.get(args.node()));
            }
        }));
    }

    @Override
    public int priority() {
        return -5;
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TValue annotation = declaredField.getAnnotation(TValue.class);
            if (annotation == null || declaredField.getType().equals(plugin.getClass())) {
                continue;
            }
            Ref.forcedAccess(declaredField);
            TValueTask tValueTask = injectTypes.get(declaredField.getType());
            if (tValueTask != null) {
                TInjectHelper.getInstance(declaredField, pluginClass, plugin).forEach(instance -> {
                    inject(plugin, declaredField, instance, annotation, tValueTask, pluginClass);
                });
            }
        }
    }

    public void inject(Plugin plugin, Field field, Object instance, TValue annotation, TValueTask tValueTask, Class<?> pluginClass) {
        try {
            tValueTask.run(plugin, field, annotation, pluginClass, instance);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
