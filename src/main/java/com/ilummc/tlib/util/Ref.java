package com.ilummc.tlib.util;

import com.google.gson.annotations.SerializedName;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.asm.AsmAnalyser;
import me.skymc.taboolib.Main;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import sun.reflect.Reflection;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Field;
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

            // 特殊判断
            if (clazz == TLib.class) {
                return Arrays.asList(clazz.getDeclaredFields());
            }

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

    public static JavaPlugin getCallerPlugin(Class<?> callerClass) {
        try {
            Field pluginField = callerClass.getClassLoader().getClass().getDeclaredField("plugin");
            pluginField.setAccessible(true);
            return (JavaPlugin) pluginField.get(callerClass.getClassLoader());
        } catch (Exception ignored) {
            TLocale.Logger.error("LOCALE.CALLER-PLUGIN-NOT-FOUND", callerClass.getName());
        }
        return (JavaPlugin) Main.getInst();
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
