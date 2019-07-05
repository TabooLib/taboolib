package io.izzel.taboolib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-16
 */
public class ArrayUtil {

    public static <T> int indexOf(T[] array, T obj) {
        return array == null || array.length == 0 ? -1 : IntStream.range(0, array.length).filter(i -> array[i] != null && array[i].equals(obj)).findFirst().orElse(-1);
    }

    public static <T> boolean contains(T[] array, T obj) {
        return indexOf(array, obj) != -1;
    }

    @SafeVarargs
    public static <T> T[] asArray(T... args) {
        return args;
    }

    public static String arrayJoin(String[] args, int start) {
        return IntStream.range(start, args.length).mapToObj(i -> args[i] + " ").collect(Collectors.joining()).trim();
    }

    public static <T> T[] arrayAppend(T[] array, T obj) {
        T[] arrayNew = arrayExpand(array, 1);
        arrayNew[array.length] = obj;
        return arrayNew;
    }

    public static <T> T[] arrayAddFirst(T[] array, T obj) {
        T[] arrayNew = arrayExpandAtFirst(array, 1);
        arrayNew[0] = obj;
        return arrayNew;
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T> T arrayExpand(T oldArray, int expand) {
        int length = java.lang.reflect.Array.getLength(oldArray);
        Object newArray = java.lang.reflect.Array.newInstance(oldArray.getClass().getComponentType(), length + expand);
        System.arraycopy(oldArray, 0, newArray, 0, length);
        return (T) newArray;
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T> T arrayExpandAtFirst(T oldArray, int expand) {
        int length = java.lang.reflect.Array.getLength(oldArray);
        Object newArray = java.lang.reflect.Array.newInstance(oldArray.getClass().getComponentType(), length + expand);
        System.arraycopy(oldArray, 0, newArray, expand, length);
        return (T) newArray;
    }

    public static <T> T skipEmpty(T obj) {
        return skipEmpty(obj, null);
    }

    public static <T> T[] skipEmpty(T[] obj) {
        return skipEmpty(obj, null);
    }

    public static <T> T skipEmpty(T obj, T def) {
        return Strings.isEmpty(String.valueOf(obj)) ? def : obj;
    }

    public static <T> T[] skipEmpty(T[] obj, T[] def) {
        if (obj.length == 0) {
            return def;
        }
        T firstElement = skipEmpty(obj[0]);
        return firstElement == null ? def : obj;
    }

    @SafeVarargs
    public static <T> List<T> asList(T... args) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, args);
        return list;
    }

    // *********************************
    //
    //           Deprecated
    //
    // *********************************

    @Deprecated
    public static String[] addFirst(String[] args, String... value) {
        if (args.length < 1) {
            return value;
        }
        List<String> list = asList(args);
        for (int i = value.length - 1; i >= 0; i--) {
            list.add(0, value[i]);
        }
        return list.toArray(new String[0]);
    }

    @Deprecated
    public static String[] removeFirst(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        List<String> list = asList(args);
        list.remove(0);
        return list.toArray(new String[0]);
    }
}
