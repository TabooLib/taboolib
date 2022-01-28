package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import taboolib.common.TabooLib;
import taboolib.common.boot.SimpleServiceLoader;

import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * TabooLib
 * taboolib.common.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
@RuntimeDependency(value = "!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
public interface RuntimeEnv {

    RuntimeEnv INSTANCE = SimpleServiceLoader.load(RuntimeEnv.class);

    void load(@NotNull Class<?> clazz);

    void loadAssets(@NotNull Class<?> clazz);

    void loadDependency(@NotNull Class<?> clazz, boolean initiative);

//    static <T> T loadService(Class<T> clazz, Supplier<T> supplier) {
//        for (T service : ServiceLoader.load(clazz, TabooLib.class.getClassLoader())) {
//            return service;
//        }
//        return supplier.get();
//    }
}