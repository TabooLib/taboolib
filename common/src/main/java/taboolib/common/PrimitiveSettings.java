package taboolib.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * TabooLib
 * taboolib.common.TabooLibSettings
 *
 * @author 坏黑
 * @since 2024/1/25 15:07
 */
public class PrimitiveSettings {

    public static final String ID = "!taboolib".substring(1);

    /**
     * 运行参数
     */
    public static final Properties RUNTIME_PROPERTIES = getProperties("env", true);

    /**
     * 版本信息
     */
    public static final Properties VERSION_PROPERTIES = getProperties("version", false);

    /**
     * Kotlin 版本
     */
    public static final String KOTLIN_VERSION = VERSION_PROPERTIES.getProperty("!kotlin".substring(1), "1.8.22");

    /**
     * Kotlinx 版本
     */
    public static final String KOTLIN_COROUTINES_VERSION = VERSION_PROPERTIES.getProperty("!kotlin-coroutines".substring(1), "1.7.3");

    /**
     * TabooLib 版本
     */
    public static final String TABOOLIB_VERSION = VERSION_PROPERTIES.getProperty(ID, "6.1.0-dev");

    /**
     * 跳过 Kotlin 重定向
     */
    public static final boolean SKIP_KOTLIN_RELOCATE = VERSION_PROPERTIES.getProperty("skip-kotlin-relocate", "false").equals("true");

    /**
     * 跳过 TabooLib 重定向
     */
    public static final boolean SKIP_TABOOLIB_RELOCATE = VERSION_PROPERTIES.getProperty("skip-taboolib-relocate", "false").equals("true");

    /**
     * 调试模式
     */
    public static final boolean IS_DEV_MODE = TABOOLIB_VERSION.endsWith("-dev");

    /**
     * 调试模式
     */
    public static final boolean IS_DEBUG_MODE = RUNTIME_PROPERTIES.getProperty("debug", "false").equals("true");

    /**
     * 是否在开发模式强制下载依赖
     */
    public static final boolean IS_FORCE_DOWNLOAD_IN_DEV_MODE = RUNTIME_PROPERTIES.getProperty("force-download-in-dev", "true").equals("true");

    /**
     * 中央仓库
     */
    public static final String REPO_CENTRAL = RUNTIME_PROPERTIES.getProperty("repo-central", "https://maven.aliyun.com/repository/central");

    /**
     * TabooLib 仓库
     */
    public static final String REPO_TABOOLIB = RUNTIME_PROPERTIES.getProperty("repo-taboolib", "http://ptms.ink:8081/repository/releases");

    /**
     * libs 位置
     */
    public static final String FILE_LIBS = RUNTIME_PROPERTIES.getProperty("file-libs", "libraries");

    /**
     * assets 位置
     */
    public static final String FILE_ASSETS = RUNTIME_PROPERTIES.getProperty("file-assets", "assets");

    /**
     * 是否启用完全隔离模式
     */
    public static boolean IS_ISOLATED_MODE = RUNTIME_PROPERTIES.getProperty("enable-isolated-classloader", "false").equals("true");

    /**
     * 使用模块
     */
    public static final String[] INSTALL_MODULES = RUNTIME_PROPERTIES.getProperty("module", "").split(",");

    /**
     * 格式化版本号
     */
    public static String formatVersion(String str) {
        return str.replaceAll("[._-]", "");
    }

    /**
     * 获取配置文件
     */
    private static Properties getProperties(String name, boolean allowGlobal) {
        boolean loaded = false;
        Properties prop = new Properties();
        // 从插件内部提取配置文件
        URL url = PrimitiveSettings.class.getClassLoader().getResource("META-INF/taboolib/" + name + ".properties");
        if (url != null) {
            try {
                prop.load(url.openStream());
                loaded = true;
            } catch (IOException ignored) {
            }
        }
        // 全局覆盖
        if (allowGlobal) {
            // 从服务端根目录中提取配置文件
            File globalFile = new File(name + ".properties");
            if (globalFile.exists()) {
                try (FileInputStream fis = new FileInputStream(globalFile)) {
                    prop.load(fis);
                    loaded = true;
                } catch (IOException ignored) {
                }
            }
        }
        // 如果加载成功
        if (loaded) {
            return prop;
        } else {
            throw new IllegalStateException("META-INF/taboolib/" + name + ".properties not found");
        }
    }
}
