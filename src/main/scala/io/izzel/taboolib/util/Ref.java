package io.izzel.taboolib.util;

import com.google.gson.annotations.SerializedName;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.util.asm.AsmAnalyser;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import sun.reflect.Reflection;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ThreadSafe
public class Ref {

    private static final Map<String, List<Field>> cachedFields = new ConcurrentHashMap<>();

    public static final int ACC_BRIDGE = 0x0040;
    public static final int ACC_SYNTHETIC = 0x1000;

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

    public static Plugin getCallerPlugin(Class<?> callerClass) {
        if (callerClass.getName().startsWith("io.izzel.taboolib") || callerClass.getName().startsWith("io.izzel.tlibscala")) {
            return TabooLib.getPlugin();
        }
        try {
            return JavaPlugin.getProvidingPlugin(callerClass);
        } catch (Exception ignored) {
            try {
                ClassLoader loader = callerClass.getClassLoader();
                Field pluginF = loader.getClass().getDeclaredField("plugin");
                pluginF.setAccessible(true);
                Object o = pluginF.get(loader);
                return (JavaPlugin) o;
            } catch (Exception e) {
                return TabooLib.getPlugin();
            }
        }
    }

    public static void forcedAccess(Field field) {
        try {
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static abstract class CallerClass {

        private static CallerClass impl;

        static {
            try {
                Class.forName("sun.reflect.Reflection");
                impl = new ReflectionImpl();
            } catch (ClassNotFoundException e) {
                impl = new StackTraceImpl();
            }
        }

        abstract Class<?> getCallerClass(int i);

        private static class ReflectionImpl extends CallerClass {

            @SuppressWarnings({"deprecation", "restriction"})
            @Override
            Class<?> getCallerClass(int i) {
                return Reflection.getCallerClass(i);
            }
        }

        private static class StackTraceImpl extends CallerClass {

            @Override
            Class<?> getCallerClass(int i) {
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                try {
                    return Class.forName(elements[i].getClassName());
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }
    }

}
