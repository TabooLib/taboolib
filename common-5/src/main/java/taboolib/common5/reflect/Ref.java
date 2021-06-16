package taboolib.common5.reflect;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Ref {

    private static final Map<String, List<Field>> cachedFields = new ConcurrentHashMap<>();
    private static final Map<String, List<Method>> cacheMethods = new ConcurrentHashMap<>();

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

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    public static MethodHandles.Lookup lookup() {
        return LOOKUP;
    }

    public static void putField(Object src, Field field, Object value) {
        try {
            MethodHandle methodHandle = lookup().unreflectSetter(field);
            if (Modifier.isStatic(field.getModifiers())) {
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
                Field[] declaredFields = clazz.getDeclaredFields();
                List<Field> list = Arrays.stream(declaredFields).filter(field -> (field.getModifiers() & excludeModifiers) == 0).collect(Collectors.toList());
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
                Method[] methods = clazz.getDeclaredMethods();
                List<Method> list = Arrays.stream(methods).filter(field -> (field.getModifiers() & excludeModifiers) == 0).collect(Collectors.toList());
                cacheMethods.putIfAbsent(clazz.getName(), list);
                return list;
            } catch (Error err) {
                return Collections.emptyList();
            }
        }
    }
}
