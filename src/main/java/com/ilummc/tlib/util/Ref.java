package com.ilummc.tlib.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.concurrent.ThreadSafe;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.util.asm.AsmAnalyser;

import sun.reflect.Reflection;

@ThreadSafe
public class Ref {

    private static final Map<String, List<Field>> cachedFields = new ConcurrentHashMap<>();

    public static final int ACC_BRIDGE = 0x0040;
    public static final int ACC_SYNTHETIC = 0x1000;

    public static List<Field> getDeclaredFields(Class<?> clazz) {
        return getDeclaredFields(clazz, 0, false);
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
            if (clazz == TLib.class)
                return Arrays.asList(clazz.getDeclaredFields());

            Class.forName("org.objectweb.asm.ClassVisitor");
            List<Field> fields;
            if ((fields = cachedFields.get(clazz.getName())) != null) return fields;
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
            if (cache) cachedFields.putIfAbsent(clazz.getName(), fields);
            return fields;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static Optional<Class<?>> getCallerClass(int depth) {
        return Optional.ofNullable(CallerClass.impl.getCallerClass(depth + 1));
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
        	
            @SuppressWarnings({ "deprecation", "restriction" })
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
