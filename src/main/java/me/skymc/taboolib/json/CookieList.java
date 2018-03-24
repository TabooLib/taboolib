package me.skymc.taboolib.json;

import java.util.Iterator;

public class CookieList {

    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONTokener x = new JSONTokener(string);
        while (x.more()) {
            String name = Cookie.unescape(x.nextTo('='));
            x.next('=');
            jo.put(name, Cookie.unescape(x.nextTo(';')));
            x.next();
        }
        return jo;
    }

    @SuppressWarnings("rawtypes")
	public static String toString(JSONObject jo) throws JSONException {
        boolean      b = false;
        Iterator     keys = jo.keys();
        String       string;
        StringBuilder sb = new StringBuilder();
        while (keys.hasNext()) {
            string = keys.next().toString();
            if (!jo.isNull(string)) {
                if (b) {
                    sb.append(';');
                }
                sb.append(Cookie.escape(string));
                sb.append("=");
                sb.append(Cookie.escape(jo.getString(string)));
                b = true;
            }
        }
        return sb.toString();
    }
}
