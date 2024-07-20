package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.ClassAppender;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.TabooLib;
import taboolib.common.env.aether.AetherResolver;
import taboolib.common.env.legacy.Artifact;
import taboolib.common.env.legacy.Dependency;
import taboolib.common.env.legacy.DependencyDownloader;
import taboolib.common.env.legacy.Repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

import static taboolib.common.PrimitiveSettings.KOTLIN_COROUTINES_VERSION;
import static taboolib.common.PrimitiveSettings.KOTLIN_VERSION;

/**
 * TabooLib
 * taboolib.common.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
@SuppressWarnings("CallToPrintStackTrace")
public class RuntimeEnv {

    public static final String KOTLIN_ID = "!kotlin".substring(1);
    public static final String KOTLIN_COROUTINES_ID = "!kotlinx.coroutines".substring(1);

    public static final RuntimeEnv ENV = new RuntimeEnv();

    private final String defaultAssets = PrimitiveSettings.FILE_ASSETS;
    private final String defaultLibrary = PrimitiveSettings.FILE_LIBS;
    private final String defaultRepositoryCentral = PrimitiveSettings.REPO_CENTRAL;

    private static boolean isAetherFound;

    static {
        // 当服务端版本在 1.17+ 时，可借助服务端自带的 Aether 库完成依赖下载，兼容性更高。
        // 同时停止对 Legacy 的支持。
        try {
            Class.forName("org.eclipse.aether.graph.Dependency");
            isAetherFound = true;
        } catch (ClassNotFoundException e) {
            isAetherFound = false;
        }
    }

    /**
     * 初始化运行时环境，由 extra.properties 调用
     * 用于初始化 Kotlin 环境
     */
    static void init() throws Throwable {
        List<JarRelocation> rel = new ArrayList<>();
        boolean loadKotlin = !KOTLIN_VERSION.equals("null");
        boolean loadKotlinCoroutines = !KOTLIN_COROUTINES_VERSION.equals("null");
        // 在非隔离模式下检查 Kotlin 环境
        if (!PrimitiveSettings.IS_ISOLATED_MODE) {
            // 非隔离模式下的重定向规则
            rel.add(new JarRelocation(KOTLIN_ID + ".", PrimitiveSettings.getRelocatedKotlinVersion() + "."));
            rel.add(new JarRelocation(KOTLIN_COROUTINES_ID + ".", PrimitiveSettings.getRelocatedKotlinCoroutinesVersion() + "."));
            // 环境检查
            if (TabooLib.isKotlinEnvironment()) loadKotlin = false;
            if (TabooLib.isKotlinCoroutinesEnvironment()) loadKotlinCoroutines = false;
        }
        // 加载 Kotlin 环境
        if (loadKotlin) ENV.loadDependency("org.jetbrains.kotlin:kotlin-stdlib:" + KOTLIN_VERSION, rel);
        // 加载 Kotlin Coroutines 环境
        if (loadKotlinCoroutines) ENV.loadDependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:" + KOTLIN_COROUTINES_VERSION, false, rel);
        PrimitiveIO.dev("RuntimeEnv initialized.");
    }

    public int inject(@NotNull Class<?> clazz) throws Throwable {
        int total = 0;
        total += loadAssets(clazz);
        total += loadDependency(clazz);
        return total;
    }

    @Nullable
    public RuntimeResource[] getAssets(@NotNull Class<?> clazz) {
        RuntimeResource[] resources = JavaAnnotation.getAnnotationsIfPresent(clazz, RuntimeResource.class);
        if (resources == null && JavaAnnotation.hasAnnotation(clazz, RuntimeResources.class)) {
            RuntimeResources annotation = JavaAnnotation.getAnnotationIfPresent(clazz, RuntimeResources.class);
            if (annotation != null) {
                resources = annotation.value();
            }
        }
        return resources;
    }

    public int loadAssets(@NotNull Class<?> clazz) throws IOException {
        int total = 0;
        RuntimeResource[] resources = getAssets(clazz);
        if (resources != null) {
            for (RuntimeResource resource : resources) {
                loadAssets(resource.name(), resource.hash(), resource.value(), resource.zip());
                total++;
            }
        }
        return total;
    }

    /**
     * 下载资源文件到 assets 目录下
     *
     * @param name 文件名
     * @param hash 文件的 SHA-1（如果是压缩包，则为原始文件的 SHA-1）
     * @param url  文件下载地址
     * @param zip  是否为压缩包格式
     */
    public void loadAssets(String name, String hash, String url, boolean zip) throws IOException {
        File file;
        if (name.isEmpty()) {
            file = new File(defaultAssets, hash.substring(0, 2) + "/" + hash);
        } else {
            file = new File(defaultAssets, name);
        }
        if (file.exists() && PrimitiveIO.getHash(file).equals(hash)) {
            return;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        PrimitiveIO.println("Downloading assets " + url.substring(url.lastIndexOf('/') + 1));
        if (zip) {
            File cacheFile = new File(file.getParentFile(), file.getName() + ".zip");
            PrimitiveIO.downloadFile(new URL(url + ".zip"), cacheFile);
            try (ZipFile zipFile = new ZipFile(cacheFile)) {
                InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(url.substring(url.lastIndexOf('/') + 1)));
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(PrimitiveIO.readFully(inputStream));
                }
            } finally {
                cacheFile.delete();
            }
        } else {
            PrimitiveIO.downloadFile(new URL(url), file);
        }
    }

    public RuntimeDependency[] getDependency(@NotNull Class<?> clazz) {
        RuntimeDependency[] dependencies = JavaAnnotation.getAnnotationsIfPresent(clazz, RuntimeDependency.class);
        if (dependencies == null && JavaAnnotation.hasAnnotation(clazz, RuntimeDependencies.class)) {
            RuntimeDependencies annotation = JavaAnnotation.getAnnotationIfPresent(clazz, RuntimeDependencies.class);
            if (annotation != null) {
                dependencies = annotation.value();
            }
        }
        return dependencies;
    }

    public int loadDependency(@NotNull Class<?> clazz) throws Throwable {
        int total = 0;
        RuntimeDependency[] dependencies = getDependency(clazz);
        if (dependencies != null) {
            File baseFile = new File(defaultLibrary);
            for (RuntimeDependency dep : dependencies) {
                String allTest = dep.test();
                List<String> tests = new ArrayList<>();
                if (allTest.contains(",")) {
                    tests.addAll(Arrays.asList(allTest.split(",")));
                } else {
                    tests.add(allTest);
                }
                if (tests.stream().allMatch(this::test)) {
                    continue;
                }
                List<JarRelocation> relocation = new ArrayList<>();
                String[] relocate = dep.relocate();
                if (relocate.length % 2 != 0) {
                    throw new IllegalStateException("invalid relocate format");
                }
                for (int i = 0; i + 1 < relocate.length; i += 2) {
                    String pattern = relocate[i].startsWith("!") ? relocate[i].substring(1) : relocate[i];
                    String relocatePattern = relocate[i + 1].startsWith("!") ? relocate[i + 1].substring(1) : relocate[i + 1];
                    relocation.add(new JarRelocation(pattern, relocatePattern));
                }
                String url = dep.value().startsWith("!") ? dep.value().substring(1) : dep.value();
                loadDependency(url, baseFile, relocation, dep.repository(), dep.ignoreOptional(), dep.ignoreException(), dep.transitive(), dep.scopes(), dep.external());
                total++;
            }
        }
        return total;
    }

    public void loadDependency(@NotNull String url) throws Throwable {
        loadDependency(url, new File(defaultLibrary));
    }

    public void loadDependency(@NotNull String url, @Nullable String repository) throws Throwable {
        loadDependency(url, new File(defaultLibrary), repository);
    }

    public void loadDependency(@NotNull String url, @NotNull List<JarRelocation> relocation) throws Throwable {
        loadDependency(url, new File(defaultLibrary), relocation, null, true, false, true, new DependencyScope[]{DependencyScope.RUNTIME, DependencyScope.COMPILE});
    }

    public void loadDependency(@NotNull String url, boolean transitive, @NotNull List<JarRelocation> relocation) throws Throwable {
        loadDependency(url, new File(defaultLibrary), relocation, null, true, false, transitive, new DependencyScope[]{DependencyScope.RUNTIME, DependencyScope.COMPILE});
    }

    public void loadDependency(@NotNull String url, @NotNull File baseDir) throws Throwable {
        loadDependency(url, baseDir, null);
    }

    public void loadDependency(@NotNull String url, @NotNull File baseDir, @Nullable String repository) throws Throwable {
        loadDependency(url, baseDir, new ArrayList<>(), repository, true, false, true, new DependencyScope[]{DependencyScope.RUNTIME, DependencyScope.COMPILE});
    }

    public void loadDependency(
            @NotNull String url,
            @NotNull File baseDir,
            @NotNull List<JarRelocation> relocation,
            @Nullable String repository,
            boolean ignoreOptional,
            boolean ignoreException,
            boolean transitive,
            @NotNull DependencyScope[] scope
    ) throws Throwable {
        loadDependency(url, baseDir, relocation, repository, ignoreOptional, ignoreException, transitive, scope, true);
    }

    public void loadDependency(
            @NotNull String url,
            @NotNull File baseDir,
            @NotNull List<JarRelocation> relocation,
            @Nullable String repository,
            boolean ignoreOptional,
            boolean ignoreException,
            boolean transitive,
            @NotNull DependencyScope[] scope,
            boolean external
    ) throws Throwable {
        // 支持用户对源进行替换
        if (repository == null || repository.isEmpty()) {
            repository = defaultRepositoryCentral;
        } else if (PrimitiveSettings.RUNTIME_PROPERTIES.containsKey("repo-" + repository)) {
            repository = PrimitiveSettings.RUNTIME_PROPERTIES.getProperty("repo-" + repository);
        }
        // 使用 Aether 处理依赖
        if (isAetherFound) {
            AetherResolver.of(repository).resolve(url, scope, transitive, ignoreOptional).forEach(file -> {
                try {
                    AetherResolver.inject(file, relocation, external);
                } catch (Throwable ex) {
                    if (!ignoreException) ex.printStackTrace();
                }
            });
        } else {
            loadDependencyLegacy(url, baseDir, relocation, repository, ignoreOptional, ignoreException, transitive, scope, external);
        }
    }

    void loadDependencyLegacy(
            @NotNull String url,
            @NotNull File baseDir,
            @NotNull List<JarRelocation> relocation,
            String repository,
            boolean ignoreOptional,
            boolean ignoreException,
            boolean transitive,
            @NotNull DependencyScope[] scope,
            boolean external
    ) throws Throwable {
        Artifact artifact = new Artifact(url);
        DependencyDownloader downloader = new DependencyDownloader(baseDir, relocation);
        downloader.addRepository(new Repository(repository));
        downloader.setIgnoreOptional(ignoreOptional);
        downloader.setIgnoreException(ignoreException);
        downloader.setDependencyScopes(scope);
        downloader.setTransitive(transitive);
        // 解析依赖
        String pomPath = String.format(
                "%s/%s/%s/%s-%s.pom",
                artifact.getGroupId().replace('.', '/'),
                artifact.getArtifactId(),
                artifact.getVersion(),
                artifact.getArtifactId(),
                artifact.getVersion()
        );
        File pomFile = new File(baseDir, pomPath);
        File pomFile1 = new File(pomFile.getPath() + ".sha1");
        // 验证文件完整性
        if (PrimitiveIO.validation(pomFile, pomFile1)) {
            downloader.loadDependencyFromInputStream(pomFile.toPath().toUri().toURL().openStream());
        } else {
            PrimitiveIO.println("Downloading library %s:%s:%s %s", artifact, transitive ? "(transitive)" : "");
            downloader.loadDependencyFromInputStream(new URL(repository + "/" + pomPath).openStream());
        }
        // 加载自身
        Dependency dep = new Dependency(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), DependencyScope.RUNTIME);
        dep.setType(artifact.getExtension());
        dep.setExternal(external);
        if (transitive) {
            downloader.injectClasspath(downloader.loadDependency(downloader.getRepositories(), dep));
        } else {
            downloader.injectClasspath(Collections.singleton(dep));
        }
    }

    private boolean test(String path) {
        String test = path.startsWith("!") ? path.substring(1) : path;
        return !test.isEmpty() && ClassAppender.isExists(test);
    }
}
