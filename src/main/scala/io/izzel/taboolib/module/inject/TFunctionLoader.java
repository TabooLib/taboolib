package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.compat.kotlin.CompatKotlin;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.plugin.Plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @Author sky
 * @Since 2018-09-08 14:00
 */
public class TFunctionLoader implements TabooLibLoader.Loader {

    @Override
    public void preLoad(Plugin plugin, Class<?> pluginClass) {
        invokeMethods(pluginClass, TFunction.Load.class);
    }

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        invokeMethods(pluginClass, true);
        invokeMethods(pluginClass, TFunction.Init.class);
    }

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        invokeMethods(pluginClass, false);
        invokeMethods(pluginClass, TFunction.Cancel.class);
    }

    public void invokeMethods(Class<?> pluginClass, boolean enable) {
        if (pluginClass.isAnnotationPresent(TFunction.class)) {
            TFunction function = pluginClass.getAnnotation(TFunction.class);
            try {
                Object instance = null;
                Method method = pluginClass.getDeclaredMethod(enable ? function.enable() : function.disable());
                if (CompatKotlin.isCompanion(pluginClass)) {
                    instance = CompatKotlin.getCompanion(pluginClass);
                    if (instance == null) {
                        TLogger.getGlobalLogger().error(method.getName() + " required @JvmStatic.");
                        return;
                    }
                } else if (!Modifier.isStatic(method.getModifiers())) {
                    TLogger.getGlobalLogger().error(method.getName() + " is not a static method.");
                    return;
                }
                method.setAccessible(true);
                method.invoke(instance);
            } catch (NoSuchMethodException ignore) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void invokeMethods(Class<?> pluginClass, Class<? extends Annotation> a) {
        for (Method declaredMethod : pluginClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(a)) {
                try {
                    Object instance = null;
                    if (CompatKotlin.isCompanion(pluginClass)) {
                        instance = CompatKotlin.getCompanion(pluginClass);
                        if (instance == null) {
                            TLogger.getGlobalLogger().error(declaredMethod.getName() + " required @JvmStatic.");
                            return;
                        }
                    } else if (!Modifier.isStatic(declaredMethod.getModifiers())) {
                        TLogger.getGlobalLogger().error(declaredMethod.getName() + " is not a static method.");
                        return;
                    }
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(instance);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
