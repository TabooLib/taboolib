package me.skymc.taboolib.itemnbtapi.utils;

import me.skymc.taboolib.TabooLib;

public class MethodNames {

    public static String getEntityNbtGetterMethodName() {
        return "b";
    }

    public static String getEntityNbtSetterMethodName() {
        return "a";
    }
    
    public static String getTileDataMethodName() {
        if (TabooLib.getVerint() <= 10800) {
            return "b";
        }
        return "save";
    }

    public static String getTypeMethodName() {
        if (TabooLib.getVerint() <= 10800) {
            return "b";
        }
        return "d";
    }

    public static String getRemoveMethodName() {
        if (TabooLib.getVerint() <= 10800) {
            return "a";
        }
        return "remove";
    }
}
