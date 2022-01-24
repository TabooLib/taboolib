package taboolib.common.boot;

import taboolib.common.TabooLib;

import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * TabooLib
 * taboolib.common.boot.SimpleServiceLoader
 *
 * @author 坏黑
 * @since 2022/1/25 2:37 AM
 */
public class SimpleServiceLoader {

    SimpleServiceLoader() {
    }

    public static <T> T load(Class<T> clazz, Supplier<T> def) {
        for (T service : ServiceLoader.load(clazz, TabooLib.class.getClassLoader())) {
            return service;
        }
        return def.get();
    }
}
