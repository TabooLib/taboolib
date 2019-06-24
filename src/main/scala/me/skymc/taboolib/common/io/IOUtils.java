package me.skymc.taboolib.common.io;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.channels.Selector;

/**
 * from org.apache.commons.io
 * support for 1.14
 */
public class IOUtils {

    public static void close(URLConnection v) {
        if (v instanceof HttpURLConnection) {
            ((HttpURLConnection)v).disconnect();
        }
    }

    public static void closeQuietly(Reader v) {
        closeQuietly((Closeable)v);
    }

    public static void closeQuietly(Writer v) {
        closeQuietly((Closeable)v);
    }

    public static void closeQuietly(InputStream v) {
        closeQuietly((Closeable)v);
    }

    public static void closeQuietly(OutputStream v) {
        closeQuietly((Closeable)v);
    }

    public static void closeQuietly(Closeable v) {
        try {
            if (v != null) {
                v.close();
            }
        } catch (IOException ignored) {
        }
    }

    public static void closeQuietly(Closeable... v) {
        if (v != null) {
            int var2 = v.length;
            for (Closeable var4 : v) {
                closeQuietly(var4);
            }
        }
    }

    public static void closeQuietly(Socket v) {
        if (v != null) {
            try {
                v.close();
            } catch (IOException ignored) {
            }
        }

    }

    public static void closeQuietly(Selector v) {
        if (v != null) {
            try {
                v.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void closeQuietly(ServerSocket v) {
        if (v != null) {
            try {
                v.close();
            } catch (IOException ignored) {
            }
        }
    }
}
