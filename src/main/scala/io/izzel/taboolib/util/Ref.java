package io.izzel.taboolib.util;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.util.asm.AsmAnalyser;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import sun.misc.Unsafe;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 反射工具
 *
 * @author izzel
 */
@SuppressWarnings("restriction")
@ThreadSafe
public class Ref {

    private static final Map<String, List<Field>> cachedFields = new ConcurrentHashMap<>();
    private static final Map<String, List<Method>> cacheMethods = new ConcurrentHashMap<>();
    private static final Map<String, Plugin> cachePlugin = new ConcurrentHashMap<>();

    private static final int ACC_BRIDGE = 0x0040;
    private static final int ACC_SYNTHETIC = 0x1000;
    private static final Unsafe UNSAFE;
    private static final MethodHandles.Lookup LOOKUP;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            UNSAFE.ensureClassInitialized(MethodHandles.Lookup.class);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = UNSAFE.staticFieldBase(lookupField);
            long lookupOffset = UNSAFE.staticFieldOffset(lookupField);
            LOOKUP = (MethodHandles.Lookup) UNSAFE.getObject(lookupBase, lookupOffset);
        } catch (Throwable t) {
            throw new IllegalStateException("Unsafe not found");
        }
    }

    /**
     * @return Unsafe 实例
     */
    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    public static MethodHandles.Lookup lookup() {
        return LOOKUP;
    }

    public static void putField(Object src, Field field, Object value) {
        Preconditions.checkNotNull(field);
        try {
            MethodHandle methodHandle = lookup().unreflectSetter(field);
            if (Modifier.isStatic(field.getModifiers())) {
                // 我怕海螺先生干我，我不写了
                methodHandle.invokeWithArguments(value);
            } else {
                methodHandle.bindTo(src).invokeWithArguments(value);
            }
        } catch (Throwable t) {
            getUnsafe().throwException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Object src, Field field, Class<T> cast) {
        Object obj = getField(src, field);
        return obj == null ? null : (T) obj;
    }

    public static Object getField(Object src, Field field) {
        Preconditions.checkNotNull(field);
        try {
            MethodHandle methodHandle = lookup().unreflectGetter(field);
            if (Modifier.isStatic(field.getModifiers())) {
                return methodHandle.invokeWithArguments();
            } else {
                return methodHandle.bindTo(src).invokeWithArguments();
            }
        } catch (Throwable t) {
            getUnsafe().throwException(t);
            return null;
        }
    }


    public static List<Field> getDeclaredFields(Class<?> clazz) {
        return getDeclaredFields(clazz, 0, true);
    }

    public static List<Field> getDeclaredFields(String clazz, int excludeModifiers, boolean cache) {
        try {
            return getDeclaredFields(Class.forName(clazz), excludeModifiers, cache);
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        }
    }

    public static List<Field> getDeclaredFields(Class<?> clazz, int excludeModifiers, boolean cache) {
        try {
            List<Field> fields;
            if ((fields = cachedFields.get(clazz.getName())) != null) {
                return fields;
            }
            ClassReader classReader = new ClassReader(clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class"));
            AsmAnalyser analyser = new AsmAnalyser(new ClassWriter(ClassWriter.COMPUTE_MAXS), excludeModifiers);
            classReader.accept(analyser, ClassReader.SKIP_DEBUG);
            fields = analyser.getFields().stream().map(name -> {
                try {
                    return clazz.getDeclaredField(name);
                } catch (Throwable ignored) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            if (cache) {
                cachedFields.putIfAbsent(clazz.getName(), fields);
            }
            return fields;
        } catch (Exception | Error e) {
            try {
                List<Field> list = Arrays.stream(clazz.getDeclaredFields())
                        .filter(field -> (field.getModifiers() & excludeModifiers) == 0).collect(Collectors.toList());
                cachedFields.putIfAbsent(clazz.getName(), list);
                return list;
            } catch (Error err) {
                return Collections.emptyList();
            }
        }
    }

    public static List<Method> getDeclaredMethods(Class<?> clazz) {
        return getDeclaredMethods(clazz, 0, true);
    }

    public static List<Method> getDeclaredMethods(String clazz, int excludeModifiers, boolean cache) {
        try {
            return getDeclaredMethods(Class.forName(clazz), excludeModifiers, cache);
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        }
    }

    public static List<Method> getDeclaredMethods(Class<?> clazz, int excludeModifiers, boolean cache) {
        try {
            List<Method> methods;
            if ((methods = cacheMethods.get(clazz.getName())) != null) {
                return methods;
            }
            ClassReader classReader = new ClassReader(clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class"));
            AsmAnalyser analyser = new AsmAnalyser(new ClassWriter(ClassWriter.COMPUTE_MAXS), excludeModifiers);
            classReader.accept(analyser, ClassReader.SKIP_DEBUG);
            methods = analyser.getMethods().stream().map(name -> {
                try {
                    return clazz.getDeclaredMethod(name);
                } catch (Throwable ignored) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            if (cache) {
                cacheMethods.putIfAbsent(clazz.getName(), methods);
            }
            return methods;
        } catch (Exception | Error e) {
            try {
                List<Method> list = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(field -> (field.getModifiers() & excludeModifiers) == 0).collect(Collectors.toList());
                cacheMethods.putIfAbsent(clazz.getName(), list);
                return list;
            } catch (Error err) {
                return Collections.emptyList();
            }
        }
    }

    public static Optional<Class<?>> getCallerClass(int depth) {
        return Optional.ofNullable(CallerClass.impl.getCallerClass(depth + 1));
    }

    public static Class<?> getCallerClassNotOptional(int depth) {
        return CallerClass.impl.getCallerClass(depth);
    }

    public static String getSerializedName(Field field) {
        return field.isAnnotationPresent(SerializedName.class) ? field.getAnnotation(SerializedName.class).value() : field.getName();
    }

    public static Optional<Field> getFieldBySerializedName(Class<?> clazz, String name) {
        for (Field field : Ref.getDeclaredFields(clazz, 0, false)) {
            if (field.isAnnotationPresent(SerializedName.class)) {
                if (field.getAnnotation(SerializedName.class).value().equals(name)) {
                    return Optional.of(field);
                } else if (field.getName().equals(name)) {
                    return Optional.of(field);
                }
            }
        }
        return Optional.empty();
    }

    public static Plugin getCallerPlugin() {
        return getCallerPlugin(getCallerClass());
    }

    public static Plugin getCallerPlugin(Class<?> callerClass) {
        if (callerClass.getName().startsWith("io.izzel.taboolib") || callerClass.getName().startsWith("io.izzel.tlibscala")) {
            return TabooLib.getPlugin();
        }
        try {
            return cachePlugin.computeIfAbsent(callerClass.getName(), n -> JavaPlugin.getProvidingPlugin(callerClass));
        } catch (Exception ignored) {
            return cachePlugin.computeIfAbsent(callerClass.getName(), n -> {
                try {
                    ClassLoader loader = callerClass.getClassLoader();
                    Object instance = getField(loader, loader.getClass().getDeclaredField("plugin"));
                    return (JavaPlugin) instance;
                } catch (Exception e) {
                    return TabooLib.getPlugin();
                }
            });
        }
    }

    public static Class<?> getCallerClass() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            try {
                Class<?> clazz = TabooLibAPI.getPluginBridge().getClass(element.getClassName());
                if (TabooLibAPI.isDependTabooLib(getCallerPlugin(clazz))) {
                    return clazz;
                }
            } catch (Throwable ignored) {
            }
        }
        return TabooLib.class;
    }

    public static void forcedAccess(Field field) {
        try {
            field.setAccessible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static abstract class CallerClass {

        private static CallerClass impl;

        static {
//            try {
//                Class.forName("sun.reflect.Reflection");
//                impl = new ReflectionImpl();
//            } catch (ClassNotFoundException e) {
//                impl = new StackTraceImpl();
//            }
            impl = new StackTraceImpl();
        }

        abstract Class<?> getCallerClass(int i);

        /**
         * Removed on Java 11
         */
        private static class ReflectionImpl extends CallerClass {

            @SuppressWarnings({"deprecation", "restriction"})
            @Override
            Class<?> getCallerClass(int i) {
//                return Reflection.getCallerClass(i);
                return null;
            }
        }

        private static class StackTraceImpl extends CallerClass {

            @Override
            Class<?> getCallerClass(int i) {
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                String className = elements[i].getClassName();
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException ignored) {
                }
                try {
                    return TabooLibAPI.getPluginBridge().getClass(className);
                } catch (NullPointerException | ClassNotFoundException ignored) {
                }
                return null;
            }
        }
    }
}
