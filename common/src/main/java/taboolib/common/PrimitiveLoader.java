package taboolib.common;

import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import taboolib.common.classloader.Precondition;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static taboolib.common.PrimitiveSettings.*;

/**
 * 原始加载器，必须在 IsolatedClassLoader 中加载
 *
 * @author 坏黑
 * @since 2024/1/24 20:29
 */
@SuppressWarnings({"DataFlowIssue", "CallToPrintStackTrace"})
public class PrimitiveLoader {

    static {
        Precondition.onlyIsolated(PrimitiveLoader.class);
    }

    public static final String TABOOLIB_GROUP = "!io.izzel.taboolib".substring(1);

    public static final String TABOOLIB_PACKAGE_NAME = "taboolib";

    public static final String PROJECT_PACKAGE_NAME = "taboolib".substring(0, "taboolib".length() - 9);

    public static final String TABOOPROJECT_GROUP = "!org.tabooproject".substring(1);

    /**
     * 基础依赖（隔离加载）
     */
    public static final String[][] DEPENDENCIES = {
            {"me.lucko", "jar-relocator", "1.5"},
            {"org.ow2.asm", "asm", "9.6"},
            {"org.ow2.asm", "asm-util", "9.6"},
            {"org.ow2.asm", "asm-commons", "9.6"}
    };

    /**
     * 默认的重定向规则
     * + taboolib -> {package}.taboolib
     * + org.tabooproject -> {package}.taboolib.library
     */
    public static String[][] defaultRelocateRule() {
        if (SKIP_TABOOLIB_RELOCATE) {
            return new String[][]{{TABOOPROJECT_GROUP, TABOOLIB_PACKAGE_NAME + ".library"}};
        } else {
            return new String[][]{{PrimitiveSettings.ID, TABOOLIB_PACKAGE_NAME}, {TABOOPROJECT_GROUP, TABOOLIB_PACKAGE_NAME + ".library"}};
        }
    }

    /**
     * 初始化各模块
     * 该方法必须在 IsolatedClassLoader 中运行，才能确保从 ClassAppender 初始化的 TabooLib 类为隔离状态
     * <p>
     * ClassLoader loader = TabooLib.class.getClassLoader();
     * if (loader instanceof IsolatedClassLoader) { ... }
     */
    public static void init() throws Throwable {
        // 开发版本
        if (IS_DEBUG_MODE) {
            // 提示
            PrimitiveIO.println("[TabooLib] \"%s\" is running in development mode.", PrimitiveIO.getRunningFileName());
        }
        // 加载基础依赖
        for (String[] i : DEPENDENCIES) load(REPO_CENTRAL, i[0], i[1], i[2], true, true, new String[][]{});
        // 加载反射模块
        load(REPO_TABOOLIB, TABOOPROJECT_GROUP + ".reflex", "reflex", "1.0.19", IS_ISOLATED_MODE, true, defaultRelocateRule());
        load(REPO_TABOOLIB, TABOOPROJECT_GROUP + ".reflex", "analyser", "1.0.19", IS_ISOLATED_MODE, true, defaultRelocateRule());
        // 加载完整模块
        loadAll();
    }

    /**
     * 从仓库下载模块并加载
     *
     * @param repo       仓库地址
     * @param group      组
     * @param name       构件名
     * @param version    版本
     * @param isIsolated 是否进入沙盒
     * @param isExternal 是否属于外部库（不会扫描类）
     */
    public static boolean load(String repo, String group, String name, String version, boolean isIsolated, boolean isExternal, String[][] relocate) throws Throwable {
        if (name.isEmpty()) return false;
        File envFile = new File(getLibraryFile(), String.format("%s/%s/%s-%s.jar", group.replace(".", "/"), name, name, version));
        File shaFile = new File(getLibraryFile(), String.format("%s/%s/%s-%s.jar.sha1", group.replace(".", "/"), name, name, version));
        // 检查文件有效性
        if (!PrimitiveIO.validation(envFile, shaFile) || (IS_FORCE_DOWNLOAD_IN_DEV_MODE && IS_DEV_MODE)) {
            try {
                PrimitiveIO.println("Downloading library %s:%s:%s", group, name, version);
                // 获取地址
                String url = String.format("%s/%s/%s/%s/%s-%s.jar", repo, group.replace(".", "/"), name, version, name, version);
                // 下载资源
                PrimitiveIO.downloadFile(new URL(url), envFile);
                PrimitiveIO.downloadFile(new URL(url + ".sha1"), shaFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            // 检查合法性
            if (!PrimitiveIO.validation(envFile, shaFile)) {
                PrimitiveIO.println("[TabooLib] Failed to download " + name + "-" + version + ".jar");
                return false;
            }
        }
        // 加载
        loadFile(envFile, isIsolated, isExternal, relocate);
        return true;
    }

    /**
     * 加载完整模块
     */
    private static void loadAll() throws Throwable {
        String[][] rule = defaultRelocateRule();
        // 加载 env 启动 Kotlin 环境
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-env", TABOOLIB_VERSION, IS_ISOLATED_MODE, true, rule);
        // 加载 util 注册 ClassAppender Callback 回调函数
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-util", TABOOLIB_VERSION, IS_ISOLATED_MODE, true, rule);
        // 加载剩余模块 >> 此时 isExternal 参数才有实际作用
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-legacy-api", TABOOLIB_VERSION, IS_ISOLATED_MODE, false, rule);
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-platform-api", TABOOLIB_VERSION, IS_ISOLATED_MODE, false, rule);
        // 加载自选模块
        for (String i : INSTALL_MODULES) load(REPO_TABOOLIB, TABOOLIB_GROUP, i, TABOOLIB_VERSION, IS_ISOLATED_MODE, false, rule);
    }

    public static void loadFile(File file, boolean isIsolated, boolean isExternal, String[][] relocate) throws Throwable {
        File jar = file;
        // 确保在 jar-relocator 加载后运行 >> java.lang.NoClassDefFoundError
        if (relocate.length > 0) {
            List<Relocation> rel = Arrays.stream(relocate).map(r -> new Relocation(r[0], r[1])).collect(Collectors.toList());
            // 在非隔离模式下进行 Kotlin 重定向
            if (!IS_ISOLATED_MODE && !SKIP_KOTLIN_RELOCATE) {
                String kt = "!kotlin".substring(1);
                String ktx = "!kotlinx.coroutines".substring(1);
                String kv = KOTLIN_VERSION.replace(".", "");
                String kvx = KOTLINX_VERSION.replace(".", "");
                rel.add(new Relocation(kt + ".", kt + kv + "."));
                rel.add(new Relocation(ktx + ".", ktx + kvx + "."));
            }
            // 是否重定向
            if (!rel.isEmpty()) {
                String hash = PrimitiveIO.getHash(file.getName() + Arrays.deepHashCode(relocate) + KOTLIN_VERSION + KOTLINX_VERSION);
                jar = new File(getCacheFile(), hash + ".jar");
                if ((!jar.exists() && jar.length() == 0) || IS_FORCE_DOWNLOAD_IN_DEV_MODE) {
                    PrimitiveIO.println("Relocating ...");
                    jar.getParentFile().mkdirs();
                    new JarRelocator(PrimitiveIO.copyFile(file, File.createTempFile(file.getName(), ".jar")), jar, rel).run();
                }
            }
        }
        ClassLoader loader = ClassAppender.addPath(jar.toPath(), isIsolated, isExternal);
        // 读取 "META-INF/taboolib/extra.properties"
        try (JarFile jarFile = new JarFile(jar)) {
            JarEntry extra = jarFile.getJarEntry("META-INF/taboolib/extra.properties");
            if (extra != null) {
                Properties extraProps = new Properties();
                extraProps.load(jarFile.getInputStream(extra));
                // 获取主类
                String main = extraProps.getProperty("main");
                String mainMethod = extraProps.getProperty("main-method");
                if (main != null && mainMethod != null) {
                    for (String cls : main.split(",")) {
                        Class<?> mainClass = Class.forName(TABOOLIB_PACKAGE_NAME + "." + cls, true, loader);
                        // 反射调用初始化函数
                        Method declaredMethod = mainClass.getDeclaredMethod(mainMethod);
                        declaredMethod.setAccessible(true);
                        declaredMethod.invoke(null);
                    }
                }
            }
        }
    }

    /**
     * 获取缓存路径
     */
    private static File getCacheFile() {
        File file = new File("cache/taboolib/" + PROJECT_PACKAGE_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取文件保存路径
     */
    private static File getLibraryFile() {
        File file = new File(FILE_LIBS);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
