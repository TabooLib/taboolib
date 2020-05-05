package io.izzel.taboolib.common.loader;

import com.google.common.collect.Lists;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.common.listener.ListenerCommand;
import io.izzel.taboolib.module.inject.TInjectHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @Author sky
 * @Since 2020-03-24 23:54
 */
@SuppressWarnings("rawtypes")
public class StartupLoader {

    static List<Class<?>> classList = Lists.newArrayList();

    static {
        StartupLoader.register(ListenerCommand.class);
    }

    public static void register(Class<?> clazz) {
        classList.add(clazz);
    }

    public static void onLoading() {
        run(Startup.Loading.class);
    }

    public static void onStarting() {
        run(Startup.Starting.class);
    }

    static void run(Class<? extends Annotation> annotation) {
        for (Class pluginClass : classList) {
            for (Method declaredMethod : pluginClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(annotation)) {
                    declaredMethod.setAccessible(true);
                    for (Object instance : TInjectHelper.getInstance(declaredMethod, pluginClass, TabooLib.getPlugin())) {
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
}
