package taboolib.common.boot;

import org.jetbrains.annotations.Nullable;
import taboolib.common.TabooLib;
import taboolib.common.io.SignatureKt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * TabooLib
 * taboolib.common.boot.SimpleServiceLoader
 *
 * @author 坏黑
 * @since 2022/1/25 2:37 AM
 */
public class SimpleServiceLoader {

    private static final String PREFIX = "META-INF/services-default/";

    SimpleServiceLoader() {
    }

    public static <T> T load(Class<T> clazz) {
        for (T service : ServiceLoader.load(clazz, TabooLib.class.getClassLoader())) {
            return service;
        }
        String id = SignatureKt.getTaboolibId();
        List<String> files = new ArrayList<>();
        List<String> found = new ArrayList<>();
        // 如果包名存在「taboolib」关键字则尝试获取原始路径
        // 例如「org.tabooproject.lores.taboolib.common.boot.Booster」将会获取「taboolib.common.boot.Booster」
        if (clazz.getName().contains(id)) {
            T service = parse(id + substringAfter(clazz.getName(), id), true, files, found);
            if (service != null) {
                return service;
            }
        }
        // 获取完整路径
        T service = parse(clazz.getName(), false, files, found);
        if (service != null) {
            return service;
        }
        throw new IllegalStateException("No implementation: " + clazz.getName() + ", files: " + files + ", found: " + found);
    }

    @SuppressWarnings("unchecked")
    static <T> T parse(String file, Boolean tabooLib, List<String> files, List<String> found) {
        try {
            files.add(PREFIX + file);
            Enumeration<URL> configs = TabooLib.class.getClassLoader().getResources(PREFIX + file);
            if (configs.hasMoreElements()) {
                String className = parse(configs.nextElement());
                // 如果是寻找「taboolib」下的服务且存在「groupId」则重定向
                // 例如「taboolib.internal.SimpleBooster」将会变成「org.tabooproject.lores.taboolib.internal.SimpleBooster」
                if (tabooLib && SignatureKt.getGroupId() != null) {
                    className = SignatureKt.getGroupId() + "." + className;
                }
                found.add(className);
                return (T) Class.forName(className).getDeclaredConstructor().newInstance();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static String substringAfter(String str, String delimiter) {
        int index = str.indexOf(delimiter);
        return index == -1 ? str : str.substring(index + delimiter.length());
    }

    @Nullable
    static String parse(URL u) throws ServiceConfigurationError {
        try (InputStream in = u.openStream(); BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return r.readLine();
        } catch (IOException x) {
            x.printStackTrace();
        }
        return null;
    }
}
