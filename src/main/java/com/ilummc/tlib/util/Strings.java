package com.ilummc.tlib.util;

public class Strings {

    /**
     * 优化过的 String#replace，比默认快了大概 5 倍
     *
     * @param template 模板替换文件
     * @param args     替换的参数
     * @return 替换好的字符串
     */
    public static String replaceWithOrder(String template, String... args) {
        if (args.length == 0 || template.length() == 0) return template;
        char[] arr = template.toCharArray();
        StringBuilder stringBuilder = new StringBuilder(template.length());
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '{' && Character.isDigit(arr[Math.min(i + 1, arr.length - 1)])
                    && arr[Math.min(i + 1, arr.length - 1)] - '0' < args.length
                    && arr[Math.min(i + 2, arr.length - 1)] == '}') {
                stringBuilder.append(args[arr[i + 1] - '0']);
                i += 2;
            } else
                stringBuilder.append(arr[i]);
        }
        return stringBuilder.toString();
    }
}
