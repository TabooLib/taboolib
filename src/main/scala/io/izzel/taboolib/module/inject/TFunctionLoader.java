package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import org.bukkit.plugin.Plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @Author sky
 * @Since 2018-09-08 14:00
 */
public class TFunctionLoader implements TabooLibLoader.Loader {

    @Override
    public void preLoad(Plugin plugin, Class<?> pluginClass) {
        invokeMethods(plugin, pluginClass, TFunction.Load.class);
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        invokeMethods(plugin, pluginClass, true);
        invokeMethods(plugin, pluginClass, TFunction.Init.class);
    }

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        invokeMethods(plugin, pluginClass, false);
        invokeMethods(plugin, pluginClass, TFunction.Cancel.class);
    }

    public void invokeMethods(Plugin plugin, Class<?> pluginClass, boolean enable) {
        if (pluginClass.isAnnotationPresent(TFunction.class)) {
            TFunction function = pluginClass.getAnnotation(TFunction.class);
            try {
                Method method = pluginClass.getDeclaredMethod(enable ? function.enable() : function.disable());
                method.setAccessible(true);
                for (Object instance : TInjectHelper.getInstance(method, pluginClass, plugin)) {
                    try {
                        method.invoke(instance);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } catch (NoSuchMethodException ignore) {
            }
        }
    }

    public void invokeMethods(Plugin plugin, Class<?> pluginClass, Class<? extends Annotation> a) {
        for (Method declaredMethod : pluginClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(a)) {
                declaredMethod.setAccessible(true);
                for (Object instance : TInjectHelper.getInstance(declaredMethod, pluginClass, plugin)) {
                    try {
                        declaredMethod.invoke(instance);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }
}
