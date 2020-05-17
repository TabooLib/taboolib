package io.izzel.taboolib.util.lite;

import org.bukkit.util.NumberConversions;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author 坏黑
 * @Since 2019-07-05 19:02
 */
public class Numbers {

    private static final DecimalFormat doubleFormat = new DecimalFormat("#.##");

    public static int toInt(Object in) {
        return NumberConversions.toInt(in);
    }

    public static long toLong(Object in) {
        return NumberConversions.toLong(in);
    }

    public static short toShort(Object in) {
        return NumberConversions.toShort(in);
    }

    public static float toFloat(Object in) {
        return NumberConversions.toFloat(in);
    }

    public static double toDouble(Object in) {
        return NumberConversions.toDouble(in);
    }

    public static byte toByte(Object in) {
        return NumberConversions.toByte(in);
    }

    public static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    public static boolean random(double v) {
        return ThreadLocalRandom.current().nextDouble() <= v;
    }

    public static int random(int v) {
        return ThreadLocalRandom.current().nextInt(v);
    }

    public static Double format(Double num) {
        return Double.valueOf(doubleFormat.format(num));
    }

    public static int getRandomInteger(Number num1, Number num2) {
        return ThreadLocalRandom.current().nextInt(num1.intValue(), num2.intValue() + 1);
    }

    public static double getRandomDouble(Number num1, Number num2) {
        return ThreadLocalRandom.current().nextDouble(num1.doubleValue(), num2.doubleValue());
    }

    public static Boolean getBoolean(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        char var = str.charAt(0);
        if (var == 'y' || var == 'Y' || var == 't' || var == 'T' || var == '1') {
            return true;
        }
        if (var == 'n' || var == 'N' || var == 'f' || var == 'F' || var == '0') {
            return false;
        }
        return false;
    }
}
