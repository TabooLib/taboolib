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
            {"org.ow2.asm", "asm", "9.4"},
            {"org.ow2.asm", "asm-util", "9.4"},
            {"org.ow2.asm", "asm-commons", "9.4"}
    };

    /**
     * 默认的重定向规则
     * + taboolib -> {package}.taboolib
     * + org.tabooproject -> {package}.taboolib.library
     */
    public static final String[][] DEF_RELOCATE = {{PrimitiveSettings.ID, TABOOLIB_PACKAGE_NAME}, {"org.tabooproject", TABOOLIB_PACKAGE_NAME + ".library"}};

    /**
     * 初始化各模块
     * 该方法必须在 IsolatedClassLoader 中运行，才能确保从 ClassAppender 初始化的 TabooLib 类为隔离状态
     * <p>
     * ClassLoader loader = TabooLib.class.getClassLoader();
     * if (loader instanceof IsolatedClassLoader) { ... }
     */
    public static void init() throws Throwable {
        // 开发版本
        if (TABOOLIB_VERSION.endsWith("-dev")) {
            // 移除缓存
            File[] relocateFile = new File(getSaveFolder(), "cache/" + PROJECT_PACKAGE_NAME).listFiles();
            if (relocateFile != null) {
                Arrays.stream(relocateFile).forEach(File::delete);
            }
            // 提示
            PrimitiveIO.println("[TabooLib] \"%s\" is running in development mode.", PrimitiveIO.getRunningFileName());
        }
        // 加载基础依赖
        for (String[] i : DEPENDENCIES) load(REPO_CENTRAL, i[0], i[1], i[2], true, true, new String[][]{});
        // 加载反射模块
        load(REPO_TABOOLIB, TABOOPROJECT_GROUP + ".reflex", "reflex", "1.0.19", IS_ISOLATED_MODE, true, DEF_RELOCATE);
        load(REPO_TABOOLIB, TABOOPROJECT_GROUP + ".reflex", "analyser", "1.0.19", IS_ISOLATED_MODE, true, DEF_RELOCATE);
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
        File envFile = new File(getSaveFolder(), name + "/" + version + "/" + name + "-" + version + ".jar");
        File shaFile = new File(getSaveFolder(), name + "/" + version + "/" + name + "-" + version + ".jar.sha1");
        // 检查文件有效性
        if (version.endsWith("-dev") || !PrimitiveIO.validation(envFile, shaFile)) {
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
        // 加载 env 启动 Kotlin 环境
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-env", TABOOLIB_VERSION, IS_ISOLATED_MODE, true, DEF_RELOCATE);
        // 加载 util 注册 ClassAppender Callback 回调函数
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-util", TABOOLIB_VERSION, IS_ISOLATED_MODE, true, DEF_RELOCATE);
        // 加载剩余模块
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-5", TABOOLIB_VERSION, IS_ISOLATED_MODE, true, DEF_RELOCATE);
        load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-env-api", TABOOLIB_VERSION, IS_ISOLATED_MODE, true, DEF_RELOCATE);
        // TODO
        // load(REPO_TABOOLIB, TABOOLIB_GROUP, "common-platform-api", TABOOLIB_VERSION, IS_ISOLATED_MODE, true, DEF_RELOCATE);
        // 加载自选模块
        for (String i : INSTALL_MODULES) load(REPO_TABOOLIB, TABOOLIB_GROUP, i, TABOOLIB_VERSION, IS_ISOLATED_MODE, true, DEF_RELOCATE);
    }

    public static void loadFile(File file, boolean isIsolated, boolean isExternal, String[][] relocate) throws Throwable {
        File jar = file;
        // 是否重定向
        if (relocate.length > 0) {
            int hash = Math.abs(Arrays.deepHashCode(relocate));
            jar = new File(getSaveFolder(), "cache/" + PROJECT_PACKAGE_NAME + "/" + file.getName() + ".rel-" + hash + ".jar");
            if (!jar.exists() && jar.length() == 0) {
                PrimitiveIO.println("Relocating ...");
                File tempSourceFile = PrimitiveIO.copyFile(file, File.createTempFile(file.getName(), ".jar"));
                List<Relocation> rel = Arrays.stream(relocate).map(r -> new Relocation(r[0], r[1])).collect(Collectors.toList());
                jar.getParentFile().mkdirs();
                new JarRelocator(tempSourceFile, jar, rel).run();
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
                    Class<?> mainClass = Class.forName(TABOOLIB_PACKAGE_NAME + "." + main, true, loader);
                    // 反射调用初始化函数
                    Method declaredMethod = mainClass.getDeclaredMethod(mainMethod);
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(null);
                }
            }
        }
    }

    /**
     * 获取文件保存路径
     */
    private static File getSaveFolder() {
        File file = new File(FILE_LIBS + "/" + PrimitiveSettings.ID);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
