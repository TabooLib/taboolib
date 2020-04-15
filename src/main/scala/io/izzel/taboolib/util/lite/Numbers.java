package io.izzel.taboolib.util.lite;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * @Author 坏黑
 * @Since 2019-07-05 19:02
 */
public class Numbers {

    private static Random random = new Random();
    private static DecimalFormat doubleFormat = new DecimalFormat("#.##");

    public static Random getRandom() {
        return random;
    }

    public static boolean random(double v) {
        return random.nextDouble() <= v;
    }

    public static int random(int v) {
        return random.nextInt(v);
    }

    public static Double format(Double num) {
        return Double.valueOf(doubleFormat.format(num));
    }

    public static int getRandomInteger(Number num1, Number num2) {
        int min = Math.min(num1.intValue(), num2.intValue());
        int max = Math.max(num1.intValue(), num2.intValue());
        return (int) (random.nextDouble() * (max - min) + min);
    }

    public static double getRandomDouble(Number num1, Number num2) {
        double min = Math.min(num1.doubleValue(), num2.doubleValue());
        double max = Math.max(num1.doubleValue(), num2.doubleValue());
        return random.nextDouble() * (max - min) + min;
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
