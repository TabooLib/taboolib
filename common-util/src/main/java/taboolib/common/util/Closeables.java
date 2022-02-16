package taboolib.common.util;

import java.io.Closeable;
import java.io.IOException;

public class Closeables {
    public static <T extends Closeable> T closeSafely(T it) {
        try {
            it.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return it;
    }
}
