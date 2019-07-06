package io.izzel.taboolib.module.lite;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2018-10-25 22:51
 */
public class SimpleReflection {

    private static Map<String, Map<String, Field>> fieldCached = Maps.newHashMap();

    public static boolean isExists(Class<?> nmsClass) {
        return fieldCached.containsKey(nmsClass.getName());
    }

    public static Map<String, Field> getFields(Class<?> nmsClass) {
        return fieldCached.getOrDefault(nmsClass.getName(), Maps.newHashMap());
    }

    public static Field getField(Class<?> nmsClass, String fieldName) {
        return fieldCached.getOrDefault(nmsClass.getName(), Maps.newHashMap()).get(fieldName);
    }

    public static void checkAndSave(Class<?> nmsClass) {
        if (!isExists(nmsClass)) {
            saveField(nmsClass);
        }
    }

    public static void saveField(Class<?> nmsClass) {
        try {
            Arrays.stream(nmsClass.getDeclaredFields()).forEach(declaredField -> saveField(nmsClass, declaredField.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveField(Class<?> nmsClass, String fieldName) {
        try {
            Field declaredField = nmsClass.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            fieldCached.computeIfAbsent(nmsClass.getName(), name -> Maps.newHashMap()).put(fieldName, declaredField);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFieldValue(Class<?> nmsClass, Object instance, String fieldName, Object value) {
        try {
            Map<String, Field> fields = fieldCached.get(nmsClass.getName());
            if (fields == null) {
                return;
            }
            Field field = fields.get(fieldName);
            if (value == null) {
                return;
            }
            field.set(instance, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getFieldValue(Class<?> nmsClass, Object instance, String fieldName) {
        try {
            Map<String, Field> fields = fieldCached.get(nmsClass.getName());
            if (fields == null) {
                return null;
            }
            Field field = fields.get(fieldName);
            if (field == null) {
                return null;
            }
            return field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getFieldValue(Class<?> nmsClass, Object instance, String fieldName, T def) {
        try {
            Map<String, Field> fields = fieldCached.get(nmsClass.getName());
            if (fields == null) {
                return def;
            }
            Field field = fields.get(fieldName);
            if (field == null) {
                return def;
            }
            return (T) field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    public static Class getListType(Field field)  {
        Type genericType = field.getGenericType();
        try {
            if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
                for (Type actualTypeArgument : ((ParameterizedType) genericType).getActualTypeArguments()) {
                    return Class.forName(actualTypeArgument.getTypeName());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static Class[] getMapType(Field field)  {
        Class[] mapType = new Class[2];
        try {
            Type genericType = field.getGenericType();
            if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
                for (Type actualTypeArgument : ((ParameterizedType) genericType).getActualTypeArguments()) {
                    mapType[mapType[0] == null ? 0 : 1] = Class.forName(actualTypeArgument.getTypeName());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return mapType[1] == null ? null : mapType ;
    }
}
