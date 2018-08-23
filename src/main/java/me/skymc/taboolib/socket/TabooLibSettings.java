package me.skymc.taboolib.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @Author sky
 * @Since 2018-08-22 23:55
 */
public class TabooLibSettings {

    private static Charset charset = Charset.forName("UTF-8");
    private static Properties settings = new Properties();
    private static Throwable throwable;

    public static boolean load() {
        try {
            settings.load(getSettingsInputStream());
            return true;
        } catch (Throwable e) {
            throwable = e;
            return false;
        }
    }

    public static InputStream getSettingsInputStream() {
        try {
            URL url = TabooLibServer.class.getClassLoader().getResource("settings.properties");
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static Properties getSettings() {
        return settings;
    }

    public static Throwable getThrowable() {
        return throwable;
    }

    public static Charset getCharset() {
        return charset;
    }
}
