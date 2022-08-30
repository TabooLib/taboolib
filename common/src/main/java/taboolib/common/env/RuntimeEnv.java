package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.TabooLibCommon;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * TabooLib
 * taboolib.common.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
public class RuntimeEnv {

    public static final RuntimeEnv ENV = new RuntimeEnv();

    private static final String ENV_FILE_NAME = "env.properties";
    private static final Properties ENV_PROPERTIES = new Properties();

    private static String defaultAssets = "assets";
    private static String defaultLibrary = "libs";
    private static String defaultRepositoryCentral = "https://maven.aliyun.com/repository/central";

    RuntimeEnv() {
        try {
            File libs = new File("libs", "custom.txt");
            if (libs.exists()) {
                defaultLibrary = Files.readAllLines(libs.toPath(), StandardCharsets.UTF_8).get(0);
            }
            File env = new File(ENV_FILE_NAME);
            if (env.exists()) {
                ENV_PROPERTIES.load(Files.newInputStream(Paths.get(ENV_FILE_NAME)));
                defaultAssets = ENV_PROPERTIES.getProperty("assets", defaultAssets);
                defaultLibrary = ENV_PROPERTIES.getProperty("library", defaultLibrary);
                defaultRepositoryCentral = ENV_PROPERTIES.getProperty("repository-central", defaultRepositoryCentral);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setup() {
        try {
            loadDependency(KotlinEnv.class, true);
        } catch (NoClassDefFoundError ignored) {
            // 若开启 skip-kotlin-relocate 则加载原始版本
            try {
                loadDependency(KotlinEnvNoRelocate.class, true);
            } catch (NoClassDefFoundError ignored2) {
            }
        }
    }

    public void inject(@NotNull Class<?> clazz) {
        loadAssets(clazz);
        loadDependency(clazz, false);
    }

    public void loadAssets(@NotNull Class<?> clazz) {
        RuntimeResource[] resources = null;
        if (clazz.isAnnotationPresent(RuntimeResource.class)) {
            resources = clazz.getAnnotationsByType(RuntimeResource.class);
        } else {
            RuntimeResources annotation = clazz.getAnnotation(RuntimeResources.class);
            if (annotation != null) {
                resources = annotation.value();
            }
        }
        if (resources == null) {
            return;
        }
        for (RuntimeResource resource : resources) {
            loadAssets(resource.name(), resource.hash(), resource.value(), resource.zip());
        }
    }

    public void loadAssets(String name, String hash, String url, boolean zip) {
        File file;
        if (name.isEmpty()) {
            file = new File(defaultAssets, hash.substring(0, 2) + "/" + hash);
        } else {
            file = new File(defaultAssets, name);
        }
        if (file.exists() && DependencyDownloader.readFileHash(file).equals(hash)) {
            return;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        TabooLibCommon.print("Downloading assets " + url.substring(url.lastIndexOf('/') + 1));
        try {
            if (zip) {
                File cacheFile = new File(file.getParentFile(), file.getName() + ".zip");
                Repository.downloadToFile(new URL(url + ".zip"), cacheFile);
                try (ZipFile zipFile = new ZipFile(cacheFile)) {
                    InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(url.substring(url.lastIndexOf('/') + 1)));
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        fileOutputStream.write(DependencyDownloader.readFully(inputStream));
                    }
                } finally {
                    cacheFile.delete();
                }
            } else {
                Repository.downloadToFile(new URL(url), file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean test(String path) {
        String test = path.startsWith("!") ? path.substring(1) : path;
        return test.length() > 0 && ClassAppender.isExists(test);
    }

    public void loadDependency(@NotNull Class<?> clazz, boolean initiative) {
        File baseFile = new File(defaultLibrary);
        RuntimeDependency[] dependencies = null;
        if (clazz.isAnnotationPresent(RuntimeDependency.class)) {
            dependencies = clazz.getAnnotationsByType(RuntimeDependency.class);
        } else {
            RuntimeDependencies annotation = clazz.getAnnotation(RuntimeDependencies.class);
            if (annotation != null) {
                dependencies = annotation.value();
            }
        }
        if (dependencies != null) {
            for (RuntimeDependency dependency : dependencies) {
                if (dependency.initiative() && !initiative) {
                    continue;
                }
                String allTest = dependency.test();
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
                String[] relocate = dependency.relocate();
                if (relocate.length % 2 != 0) {
                    throw new IllegalArgumentException("unformatted relocate");
                }
                for (int i = 0; i + 1 < relocate.length; i += 2) {
                    String pattern = relocate[i].startsWith("!") ? relocate[i].substring(1) : relocate[i];
                    String relocatePattern = relocate[i + 1].startsWith("!") ? relocate[i + 1].substring(1) : relocate[i + 1];
                    relocation.add(new JarRelocation(pattern, relocatePattern));
                }
                try {
                    String url = dependency.value().startsWith("!") ? dependency.value().substring(1) : dependency.value();
                    loadDependency(url, baseFile, relocation, dependency.repository(), dependency.ignoreOptional(), dependency.ignoreException(), dependency.transitive(), dependency.scopes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void loadDependency(@NotNull String url) throws IOException {
        loadDependency(url, new File(defaultLibrary));
    }

    public void loadDependency(@NotNull String url, @Nullable String repository) throws IOException {
        loadDependency(url, new File(defaultLibrary), repository);
    }

    public void loadDependency(@NotNull String url, @NotNull List<JarRelocation> relocation) throws IOException {
        loadDependency(url, new File(defaultLibrary));
    }

    public void loadDependency(@NotNull String url, @NotNull File baseDir) throws IOException {
        loadDependency(url, baseDir, null);
    }

    public void loadDependency(@NotNull String url, @NotNull File baseDir, @Nullable String repository) throws IOException {
        loadDependency(url, baseDir, new ArrayList<>(), repository, true, false, true, new DependencyScope[]{DependencyScope.RUNTIME, DependencyScope.COMPILE});
    }

    public void loadDependency(@NotNull String url, @NotNull File baseDir, @NotNull List<JarRelocation> relocation, @Nullable String repository, boolean ignoreOptional, boolean ignoreException, boolean transitive, @NotNull DependencyScope[] dependencyScopes) throws IOException {
        String[] args = url.split(":");
        DependencyDownloader downloader = new DependencyDownloader(baseDir, relocation);
        // 支持用户对源进行替换
        if (repository == null || repository.isEmpty()) {
            repository = defaultRepositoryCentral;
        } else if (ENV_PROPERTIES.contains("repository-" + repository)) {
            repository = ENV_PROPERTIES.getProperty("repository-" + repository);
        }
        downloader.addRepository(new Repository(repository));
        downloader.setIgnoreOptional(ignoreOptional);
        downloader.setIgnoreException(ignoreException);
        downloader.setDependencyScopes(dependencyScopes);
        downloader.setTransitive(transitive);
        // 解析依赖
        File pomFile = new File(baseDir, String.format("%s/%s/%s/%s-%s.pom", args[0].replace('.', '/'), args[1], args[2], args[1], args[2]));
        File pomShaFile = new File(pomFile.getPath() + ".sha1");
        if (pomFile.exists() && pomShaFile.exists() && DependencyDownloader.readFile(pomShaFile).startsWith(DependencyDownloader.readFileHash(pomFile))) {
            downloader.loadDependencyFromInputStream(pomFile.toPath().toUri().toURL().openStream());
        } else {
            String pom = String.format("%s/%s/%s/%s/%s-%s.pom", repository, args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
            try {
                TabooLibCommon.print(String.format("Downloading library %s:%s:%s %s", args[0], args[1], args[2], transitive ? "(transitive)" : ""));
                downloader.loadDependencyFromInputStream(new URL(pom).openStream());
            } catch (FileNotFoundException ex) {
                if (ex.toString().contains("@kotlin_version@")) {
                    return;
                }
                throw ex;
            }
        }
        // 加载自身
        Dependency current = new Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME);
        if (transitive) {
            downloader.injectClasspath(downloader.loadDependency(downloader.getRepositories(), current));
        } else {
            downloader.injectClasspath(Collections.singleton(current));
        }
    }
}
