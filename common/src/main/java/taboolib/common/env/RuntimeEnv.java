package taboolib.common.env;

import me.lucko.jarrelocator.Relocation;
import org.jetbrains.annotations.NotNull;
import taboolib.common.TabooLibCommon;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private boolean notify = false;

    RuntimeEnv() {
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
        if (resources != null) {
            for (RuntimeResource resource : resources) {
                File file;
                if (resource.name().isEmpty()) {
                    file = new File("assets/" + resource.hash().substring(0, 2) + "/" + resource.hash());
                } else {
                    file = new File("assets/" + resource.name());
                }
                if (file.exists() && DependencyDownloader.readFileHash(file).equals(resource.hash())) {
                    continue;
                }
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    if (!notify) {
                        notify = true;
                        if (TabooLibCommon.isSysoutCatcherFound()) {
                            if (System.console() != null) {
                                System.console().printf("Loading assets, please wait...\n");
                            }
                        } else {
                            System.out.println("Loading assets, please wait...");
                        }
                    }
                    if (resource.zip()) {
                        File cacheFile = new File(file.getParentFile(), file.getName() + ".zip");
                        Repository.downloadToFile(new URL(resource.value() + ".zip"), cacheFile);
                        try (ZipFile zipFile = new ZipFile(cacheFile)) {
                            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(resource.value().substring(resource.value().lastIndexOf('/') + 1)));
                            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                                fileOutputStream.write(DependencyDownloader.readFully(inputStream));
                            }
                        } finally {
                            cacheFile.delete();
                        }
                    } else {
                        Repository.downloadToFile(new URL(resource.value()), file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadDependency(@NotNull Class<?> clazz, boolean initiative) {
        File baseDir;
        try {
            List<String> lines = Files.readAllLines(new File("libs", "custom.txt").toPath(), StandardCharsets.UTF_8);
            baseDir = new File(lines.get(0));
        } catch (Throwable e) {
            baseDir = new File("libs");
        }

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
                String test = dependency.test().startsWith("!") ? dependency.test().substring(1) : dependency.test();
                if (test.length() > 0 && ClassAppender.isExists(test)) {
                    continue;
                }
                List<Relocation> relocation = new ArrayList<>();
                String[] relocate = dependency.relocate();
                if (relocate.length % 2 != 0) {
                    throw new IllegalArgumentException("unformatted relocate");
                }
                for (int i = 0; i + 1 < relocate.length; i += 2) {
                    String pattern = relocate[i].startsWith("!") ? relocate[i].substring(1) : relocate[i];
                    String relocatePattern = relocate[i + 1].startsWith("!") ? relocate[i + 1].substring(1) : relocate[i + 1];
                    relocation.add(new Relocation(pattern, relocatePattern));
                }
                try {
                    String[] args = dependency.value().startsWith("!") ? dependency.value().substring(1).split(":") : dependency.value().split(":");
                    DependencyDownloader downloader = new DependencyDownloader(baseDir, relocation);
                    downloader.addRepository(new Repository(dependency.repository()));
                    downloader.setIgnoreOptional(dependency.ignoreOptional());
                    downloader.setDependencyScopes(dependency.scopes());
                    // 解析依赖
                    File pomFile = new File(baseDir, String.format("%s/%s/%s/%s-%s.pom", args[0].replace('.', '/'), args[1], args[2], args[1], args[2]));
                    File pomShaFile = new File(pomFile.getPath() + ".sha1");
                    if (pomFile.exists() && pomShaFile.exists() && DependencyDownloader.readFile(pomShaFile).startsWith(DependencyDownloader.readFileHash(pomFile))) {
                        downloader.loadDependencyFromInputStream(pomFile.toPath().toUri().toURL().openStream());
                    } else {
                        String pom = String.format("%s/%s/%s/%s/%s-%s.pom", dependency.repository(), args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
                        try {
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
                    Set<Dependency> dependenciesWithTransitive = downloader.loadDependency(downloader.getRepositories(), current);
                    if (dependency.transitive()) {
                        downloader.injectClasspath(dependenciesWithTransitive);
                    } else {
                        downloader.injectClasspath(Collections.singleton(current));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
