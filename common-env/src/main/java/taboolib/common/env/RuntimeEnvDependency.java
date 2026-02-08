package taboolib.common.env;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassAnnotation;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.ClassAppender;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.env.aether.AetherResolver;
import taboolib.common.env.legacy.Artifact;
import taboolib.common.env.legacy.Dependency;
import taboolib.common.env.legacy.DependencyDownloader;
import taboolib.common.env.legacy.Repository;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static taboolib.common.PrimitiveIO.t;

public class RuntimeEnvDependency {

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
        // Mohist 直接不用 Aether
        try {
            Class.forName("com.mohistmc.MohistMC");
            isAetherFound = false;
        }catch (ClassNotFoundException ignored){
        }
    }

    public List<ParsedDependency> getDependency(@NotNull ReflexClass clazz) {
        List<ParsedDependency> dependencyList = new ArrayList<>();
        ClassAnnotation runtimeDependency = clazz.getAnnotationIfPresent(RuntimeDependency.class);
        if (runtimeDependency != null) {
            dependencyList.add(new ParsedDependency(runtimeDependency.properties()));
        }
        ClassAnnotation runtimeDependencies = clazz.getAnnotationIfPresent(RuntimeDependencies.class);
        if (runtimeDependencies != null) {
            runtimeDependencies.mapList("value").forEach(map -> dependencyList.add(new ParsedDependency(map)));
        }
        return dependencyList;
    }

    public int loadDependency(@NotNull ReflexClass clazz) throws Throwable {
        int total = 0;
        List<ParsedDependency> dependencies = getDependency(clazz);
        if (dependencies != null) {
            File baseFile = new File(defaultLibrary);
            for (ParsedDependency dep : dependencies) {
                total++;
                String allTest = dep.test();
                List<String> tests = new ArrayList<>();
                if (allTest.contains(",")) {
                    tests.addAll(Arrays.asList(allTest.split(",")));
                } else {
                    tests.add(allTest);
                }
                if (!tests.isEmpty() && tests.stream().allMatch(this::test)) {
                    continue;
                }
                List<JarRelocation> relocation = new ArrayList<>();
                List<String> relocate = dep.relocate();
                if (relocate.size() % 2 != 0) {
                    throw new IllegalStateException("invalid relocate format");
                }
                for (int i = 0; i + 1 < relocate.size(); i += 2) {
                    String from = relocate.get(i);
                    String to = relocate.get(i + 1);
                    // 移除前缀
                    if (from.startsWith("!")) from = from.substring(1);
                    if (to.startsWith("!")) to = to.substring(1);
                    relocation.add(new JarRelocation(from, to));
                }
                String url = dep.value().startsWith("!") ? dep.value().substring(1) : dep.value();
                loadDependency(url, baseFile, relocation, dep.repository(), dep.ignoreOptional(), dep.ignoreException(), dep.transitive(), dep.scopes(), dep.external());
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
        loadDependency(url, new File(defaultLibrary), relocation, null, true, false, true, Arrays.asList(DependencyScope.RUNTIME, DependencyScope.COMPILE));
    }

    public void loadDependency(@NotNull String url, boolean transitive, @NotNull List<JarRelocation> relocation) throws Throwable {
        loadDependency(url, new File(defaultLibrary), relocation, null, true, false, transitive, Arrays.asList(DependencyScope.RUNTIME, DependencyScope.COMPILE));
    }

    public void loadDependency(@NotNull String url, @NotNull File baseDir) throws Throwable {
        loadDependency(url, baseDir, null);
    }

    public void loadDependency(@NotNull String url, @NotNull File baseDir, @Nullable String repository) throws Throwable {
        loadDependency(url, baseDir, new ArrayList<>(), repository, true, false, true, Arrays.asList(DependencyScope.RUNTIME, DependencyScope.COMPILE));
    }

    public void loadDependency(
            @NotNull String url,
            @NotNull File baseDir,
            @NotNull List<JarRelocation> relocation,
            @Nullable String repository,
            boolean ignoreOptional,
            boolean ignoreException,
            boolean transitive,
            @NotNull List<DependencyScope> scope
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
            @NotNull List<DependencyScope> scope,
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
            @NotNull List<DependencyScope> scope,
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
            PrimitiveIO.println(
                    t("正在下载依赖 {0}:{1}:{2} {3}", "Downloading library {0}:{1}:{2} {3}"),
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    artifact.getVersion(),
                    transitive ? t("(传递模式)", "(transitive)") : ""
            );
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

    /**
     * 从本地文件中加载依赖
     */
    @SuppressWarnings("deprecation")
    public void loadFromLocalFile(URL url) throws Throwable {
        if (url == null) {
            return;
        }
        try (InputStream inputStream = url.openStream()) {
            JsonElement parsed = new JsonParser().parse(PrimitiveIO.readFully(inputStream, StandardCharsets.UTF_8));
            if (!parsed.isJsonArray()) return;
            JsonArray array = parsed.getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject object = element.getAsJsonObject();
                // 获取检查条件
                List<String> test = new ArrayList<>();
                for (JsonElement testElement : array(object, "test")) {
                    test.add(testElement.getAsString());
                }
                if (!test.isEmpty() && test.stream().allMatch(this::test)) {
                    continue;
                }
                // 获取依赖信息
                String value = object.get("value").getAsString();
                String repository = find(object, "repository", defaultRepositoryCentral);
                boolean transitive = find(object, "transitive");
                boolean ignoreOptional = find(object, "ignoreOptional");
                boolean ignoreException = find(object, "ignoreException");
                boolean external = find(object, "external");
                // 读取依赖范围
                List<DependencyScope> scopes = new ArrayList<>();
                for (JsonElement scope : array(object, "scopes")) {
                    scopes.add(DependencyScope.valueOf(scope.getAsString().toUpperCase()));
                }
                // 读取重定向规则
                List<JarRelocation> relocation = new ArrayList<>();
                JsonArray relocate = array(object, "relocate");
                for (int i = 0; i + 1 < relocate.size(); i += 2) {
                    relocation.add(new JarRelocation(relocate.get(i).getAsString(), relocate.get(i + 1).getAsString()));
                }
                // 加载依赖
                loadDependency(value, new File(defaultLibrary), relocation, repository, ignoreOptional, ignoreException, transitive, scopes, external);
            }
        }
    }

    boolean test(String path) {
        String test = path.startsWith("!") ? path.substring(1) : path;
        return !test.isEmpty() && ClassAppender.isExists(test);
    }

    String find(JsonObject object, String key, String def) {
        return object.has(key) ? object.get(key).getAsString() : def;
    }

    boolean find(JsonObject object, String key) {
        return object.has(key) && object.get(key).getAsBoolean();
    }

    JsonArray array(JsonObject object, String key) {
        return object.has(key) ? object.getAsJsonArray(key) : new JsonArray();
    }
}
