package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.ClassInstanceKt;
import taboolib.common.platform.AwakeFunction;
import taboolib.common.platform.PlatformFactory;
import taboolib.common.platform.SkipTo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * TabooLib
 * taboolib.common.RuntimeInjector
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
public class RuntimeInjector {

    private static final TreeMap<Byte, Injectors> propertyMap = new TreeMap<>();

    static {
        try {
            for (LifeCycle value : LifeCycle.values()) {
                register(new AwakeFunction(value));
            }
        } catch (NoClassDefFoundError ignored) {
        }
    }

    public static void register(@NotNull Injector.Fields injector) {
        Injectors injectors = propertyMap.computeIfAbsent(injector.getPriority(), i -> new Injectors());
        injectors.fields.add(injector);
    }

    public static void register(@NotNull Injector.Methods injector) {
        Injectors injectors = propertyMap.computeIfAbsent(injector.getPriority(), i -> new Injectors());
        injectors.methods.add(injector);
    }

    public static void register(@NotNull Injector.Classes injector) {
        Injectors injectors = propertyMap.computeIfAbsent(injector.getPriority(), i -> new Injectors());
        injectors.classes.add(injector);
    }

    public static void injectAll(@NotNull LifeCycle lifeCycle) {
        if (TabooLibCommon.isKotlinEnvironment()) {
            List<Class<?>> classes = new ArrayList<>();
            for (Class<?> runningClass : ClassInstanceKt.getRunningClasses()) {
                if (PlatformFactory.INSTANCE.checkPlatform(runningClass)) {
                    classes.add(runningClass);
                }
            }
            for (Map.Entry<Byte, Injectors> entry : propertyMap.entrySet()) {
                for (Class<?> clazz : classes) {
                    inject(clazz, entry.getValue(), lifeCycle);
                }
            }
        }
    }

    public static void injectAll(@NotNull Class<?> clazz) {
        for (Map.Entry<Byte, Injectors> entry : propertyMap.entrySet()) {
            inject(clazz, entry.getValue(), null);
        }
    }

    public static void inject(@NotNull Class<?> clazz, @NotNull Injectors injectors, @Nullable LifeCycle lifeCycle) {
        if (TabooLibCommon.isStopped()) {
            return;
        }
        if (lifeCycle != null && clazz.isAnnotationPresent(SkipTo.class)) {
            int skip = clazz.getAnnotation(SkipTo.class).value().ordinal();
            if (skip > lifeCycle.ordinal()) {
                return;
            }
        }
        Supplier<?> instance = ClassInstanceKt.findInstance(clazz, false);
        if (instance == null) {
            return;
        }
        Field[] declaredFields;
        try {
            declaredFields = clazz.getDeclaredFields();
        } catch (NoClassDefFoundError ignored) {
            return;
        }
        Method[] declaredMethods;
        try {
            declaredMethods = clazz.getDeclaredMethods();
        } catch (NoClassDefFoundError ignored) {
            return;
        }
        for (Injector.Classes inj : injectors.classes) {
            if (lifeCycle == null || lifeCycle == inj.getLifeCycle()) {
                inj.inject(clazz, instance);
            }
        }
        for (Injector.Fields inj : injectors.fields) {
            if (lifeCycle == null || lifeCycle == inj.getLifeCycle()) {
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    inj.inject(field, clazz, instance);
                }
            }
        }
        for (Injector.Methods inj : injectors.methods) {
            if (lifeCycle == null || lifeCycle == inj.getLifeCycle()) {
                for (Method method : declaredMethods) {
                    method.setAccessible(true);
                    inj.inject(method, clazz, instance);
                }
            }
        }
        for (Injector.Classes inj : injectors.classes) {
            if (lifeCycle == null || lifeCycle == inj.getLifeCycle()) {
                inj.postInject(clazz, instance);
            }
        }
    }

    private static class Injectors {

        private final List<Injector.Classes> classes = new ArrayList<>();
        private final List<Injector.Fields> fields = new ArrayList<>();
        private final List<Injector.Methods> methods = new ArrayList<>();
    }
}
