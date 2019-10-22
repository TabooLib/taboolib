package io.izzel.taboolib.util;

import com.google.common.collect.Lists;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Strings {

    public static boolean nonBlack(String var) {
        return !isBlank(var);
    }

    public static boolean isBlank(String var) {
        return var == null || var.trim().isEmpty();
    }

    public static boolean nonEmpty(CharSequence var) {
        return !isEmpty(var);
    }

    public static boolean isEmpty(CharSequence var) {
        return var == null || var.length() == 0;
    }

    public static String replaceWithOrder(String template, String... args) {
        return replaceWithOrder(template, Lists.newArrayList(args).toArray());
    }

    /**
     * 优化过的 String#replace，比默认快了大概 5 倍
     *
     * @param template 模板替换文件
     * @param args     替换的参数
     * @return 替换好的字符串
     */
    public static String replaceWithOrder(String template, Object... args) {
        if (args.length == 0 || template.length() == 0) {
            return template;
        }
        char[] arr = template.toCharArray();
        StringBuilder stringBuilder = new StringBuilder(template.length());
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '{' && Character.isDigit(arr[Math.min(i + 1, arr.length - 1)])
                    && arr[Math.min(i + 1, arr.length - 1)] - '0' < args.length
                    && arr[Math.min(i + 2, arr.length - 1)] == '}') {
                stringBuilder.append(args[arr[i + 1] - '0']);
                i += 2;
            } else {
                stringBuilder.append(arr[i]);
            }
        }
        return stringBuilder.toString();
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    public static double similarDegree(String strA, String strB){
        String newStrA = removeSign(max(strA, strB));
        String newStrB = removeSign(min(strA, strB));
        try {
            int temp = Math.max(newStrA.length(), newStrB.length());
            int temp2 = longestCommonSubstring(newStrA, newStrB).length();
            return temp2 * 1.0 / temp;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static String max(String strA, String strB) {
        return strA.length() >= strB.length() ? strA : strB;
    }

    private static String min(String strA, String strB) {
        return strA.length() < strB.length() ? strA : strB;
    }

    private static String removeSign(String str) {
        StringBuilder builder = new StringBuilder();
        for (char item : str.toCharArray()) {
            if (charReg(item)){
                builder.append(item);
            }
        }
        return builder.toString();
    }

    private static boolean charReg(char charValue) {
        return (charValue >= 0x4E00 && charValue <= 0X9FA5) || (charValue >= 'a' && charValue <= 'z') || (charValue >= 'A' && charValue <= 'Z')  || (charValue >= '0' && charValue <= '9');
    }

    private static String longestCommonSubstring(String strA, String strB) {
        char[] chars_strA = strA.toCharArray();
        char[] chars_strB = strB.toCharArray();
        int m = chars_strA.length;
        int n = chars_strB.length;

        int[][] matrix = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (chars_strA[i - 1] == chars_strB[j - 1]) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                } else {
                    matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
                }
            }
        }

        char[] result = new char[matrix[m][n]];
        int currentIndex = result.length - 1;
        while (matrix[m][n] != 0) {
            if (matrix[n] == matrix[n - 1]) {
                n--;
            } else if (matrix[m][n] == matrix[m - 1][n]) {
                m--;
            } else {
                result[currentIndex] = chars_strA[m - 1];
                currentIndex--;
                n--;
                m--;
            }
        }
        return new String(result);
    }
}
