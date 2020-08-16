package io.izzel.taboolib.module.lite;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.util.Ref;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-10-25 22:51
 */
public class SimpleReflection {

    private static final Map<String, Map<String, Field>> fieldCached = Maps.newConcurrentMap();
    private static final Map<String, Map<String, Method>> methodCached = Maps.newConcurrentMap();

    @Deprecated
    public static boolean isExists(Class<?> nmsClass) {
        return fieldCached.containsKey(nmsClass.getName());
    }

    public static boolean isFieldExists(Class<?> nmsClass) {
        return fieldCached.containsKey(nmsClass.getName());
    }

    public static boolean isMethodExists(Class<?> nmsClass) {
        return methodCached.containsKey(nmsClass.getName());
    }

    public static Map<String, Field> getFields(Class<?> nmsClass) {
        return fieldCached.getOrDefault(nmsClass.getName(), Maps.newConcurrentMap());
    }

    public static Map<String, Method> getMethods(Class<?> nmsClass) {
        return methodCached.getOrDefault(nmsClass.getName(), Maps.newConcurrentMap());
    }

    public static Field getField(Class<?> nmsClass, String fieldName) {
        return fieldCached.getOrDefault(nmsClass.getName(), Maps.newConcurrentMap()).get(fieldName);
    }

    public static Method getMethod(Class<?> nmsClass, String methodName) {
        return methodCached.getOrDefault(nmsClass.getName(), Maps.newConcurrentMap()).get(methodName);
    }

    public static void checkAndSave(Class<?>... nmsClass) {
        Arrays.stream(nmsClass).forEach(SimpleReflection::checkAndSave);
    }

    public static void checkAndSave(Class<?> nmsClass) {
        if (!isFieldExists(nmsClass)) {
            saveField(nmsClass);
        }
        if (!isMethodExists(nmsClass)) {
            saveMethod(nmsClass);
        }
    }

    public static void saveField(Class<?>... nmsClass) {
        Arrays.stream(nmsClass).forEach(SimpleReflection::saveField);
    }

    public static void saveMethod(Class<?>... nmsClass) {
        Arrays.stream(nmsClass).forEach(SimpleReflection::saveMethod);
    }

    public static void saveField(Class<?> nmsClass) {
        try {
            Arrays.stream(nmsClass.getDeclaredFields()).forEach(declaredField -> saveField(nmsClass, declaredField.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveMethod(Class<?> nmsClass) {
        try {
            Ref.getDeclaredMethods(nmsClass).forEach(declaredMethod -> saveMethod(nmsClass, declaredMethod.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveField(Class<?> nmsClass, String fieldName) {
        try {
            Field declaredField = nmsClass.getDeclaredField(fieldName);
            fieldCached.computeIfAbsent(nmsClass.getName(), name -> Maps.newConcurrentMap()).put(fieldName, declaredField);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveMethod(Class<?> nmsClass, String methodName) {
        try {
            Method declaredMethod = nmsClass.getDeclaredMethod(methodName);
            methodCached.computeIfAbsent(nmsClass.getName(), name -> Maps.newConcurrentMap()).put(methodName, declaredMethod);
        } catch (NoSuchMethodException | NoSuchMethodError ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFieldValue(Class<?> nmsClass, Object instance, String fieldName, Object value) {
        setFieldValue(nmsClass, instance, fieldName, value, false);
    }

    public static void setFieldValue(Class<?> nmsClass, Object instance, String fieldName, Object value, boolean check) {
        if (check) {
            checkAndSave(nmsClass);
        }
        Map<String, Field> fields = fieldCached.get(nmsClass.getName());
        if (fields == null) {
            return;
        }
        Field field = fields.get(fieldName);
        if (value == null) {
            return;
        }
        try {
            Ref.putField(instance, field, value);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Object getFieldValue(Class<?> nmsClass, Object instance, String fieldName) {
        return getFieldValueChecked(nmsClass, instance, fieldName, false);
    }

    public static Object getFieldValueChecked(Class<?> nmsClass, Object instance, String fieldName, boolean check) {
        if (check) {
            checkAndSave(nmsClass);
        }
        Map<String, Field> fields = fieldCached.get(nmsClass.getName());
        if (fields == null) {
            return null;
        }
        Field field = fields.get(fieldName);
        if (field == null) {
            return null;
        }
        try {
            return Ref.getField(instance, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getFieldValue(Class<?> nmsClass, Object instance, String fieldName, T def) {
        return getFieldValue(nmsClass, instance, fieldName, def, false);
    }

    public static <T> T getFieldValue(Class<?> nmsClass, Object instance, String fieldName, T def, boolean check) {
        if (check) {
            checkAndSave(nmsClass);
        }
        Map<String, Field> fields = fieldCached.get(nmsClass.getName());
        if (fields == null) {
            return null;
        }
        Field field = fields.get(fieldName);
        if (field == null) {
            return null;
        }
        try {
            return (T) Ref.getField(instance, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    public static Object invokeMethod(Class<?> nmsClass, Object instance, String methodName, Object[] arguments) {
        return invokeMethod(nmsClass, instance, methodName, arguments, false);
    }

    public static Object invokeMethod(Class<?> nmsClass, Object instance, String methodName, Object[] arguments, boolean check) {
        if (check) {
            checkAndSave(nmsClass);
        }
        Map<String, Method> methods = methodCached.get(nmsClass.getName());
        if (methods == null) {
            return null;
        }
        Method method = methods.get(methodName);
        if (method == null) {
            return null;
        }
        try {
            method.setAccessible(true);
            return method.invoke(instance, arguments);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static Class getListType(Field field) {
        Type genericType = field.getGenericType();
        try {
            if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
                for (Type actualTypeArgument : ((ParameterizedType) genericType).getActualTypeArguments()) {
                    return TabooLibAPI.getPluginBridge().getClass(actualTypeArgument.getTypeName());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static Class[] getMapType(Field field) {
        Class[] mapType = new Class[2];
        try {
            Type genericType = field.getGenericType();
            if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
                for (Type actualTypeArgument : ((ParameterizedType) genericType).getActualTypeArguments()) {
                    mapType[mapType[0] == null ? 0 : 1] = TabooLibAPI.getPluginBridge().getClass(actualTypeArgument.getTypeName());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return mapType[1] == null ? null : mapType;
    }
}
