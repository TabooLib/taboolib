package me.skymc.taboolib.methods;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Deprecated
public class MethodsUtils {

    public static boolean checkUser(String packagename, String current) {
        return current.substring(0, 8).equals(packagename);
    }

    @SuppressWarnings("rawtypes")
    public static <T> Object[] a(T classname, String methodname, Class[] classes, Object[] objects) {
        if (!checkUser(new String(new byte[]{'m', 'e', '.', 's', 'k', 'y', 'm', 'c'}), new Exception().getStackTrace()[1].getClassName())) {
            throw new Error("未经允许的方法调用");
        }

        Class<?> clazz = classname.getClass();
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodname, classes);
            method.setAccessible(true);
            return new Object[]{method.invoke(classname, objects)};
        } catch (SecurityException | InvocationTargetException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Object b(T classname, String fieldname) {
        if (!checkUser(new String(new byte[]{'m', 'e', '.', 's', 'k', 'y', 'm', 'c'}), new Exception().getStackTrace()[1].getClassName())) {
            throw new Error("未经允许的方法调用");
        }

        Class<?> clazz = classname.getClass();
        Field field = null;
        Object object = null;
        try {
            field = clazz.getDeclaredField(fieldname);
            field.setAccessible(true);
            object = field.get(classname);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
        return object;
    }
}