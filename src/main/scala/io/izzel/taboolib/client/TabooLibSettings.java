package io.izzel.taboolib.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @Author sky
 * @Since 2018-08-22 23:55
 */
public class TabooLibSettings {

    private static final Properties settings = new Properties();
    private static final Charset charset = StandardCharsets.UTF_8;
    private static Throwable error;

    public static boolean load() {
        try {
            settings.load(getSettingsInputStream());
            return true;
        } catch (Throwable e) {
            error = e;
            return false;
        }
    }

    public static InputStream getSettingsInputStream() {
        try {
            URL url = TabooLibServer.class.getClassLoader().getResource("__resources__/settings.properties");
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

    public static Properties getSettings() {
        return settings;
    }

    public static Throwable getError() {
        return error;
    }

    public static Charset getCharset() {
        return charset;
    }
}
