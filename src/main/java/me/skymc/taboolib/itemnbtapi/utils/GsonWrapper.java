package me.skymc.taboolib.itemnbtapi.utils;

import com.google.gson.Gson;
import me.skymc.taboolib.message.MsgUtils;

public class GsonWrapper {

    private static final Gson gson = new Gson();

    public static String getString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T deserializeJson(String json, Class<T> type) {
        try {
            if (json == null) {
                return null;
            }

            T obj = gson.fromJson(json, type);
            return type.cast(obj);
        } catch (Exception ex) {
            MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

}
