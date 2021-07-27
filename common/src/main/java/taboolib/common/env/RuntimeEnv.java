package taboolib.common.env;

import me.lucko.jarrelocator.Relocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipFile;

/**
 * TabooLib
 * taboolib.common.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib:@kotlin_version@",
        test = "!taboolib.library.kotlin_@kotlin_version_escape@.KotlinVersion",
        relocate = {"!kotlin", "!taboolib.library.kotlin_@kotlin_version_escape@"}
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk7:@kotlin_version@",
        test = "!taboolib.library.kotlin_@kotlin_version_escape@.jdk7.AutoCloseableKt",
        relocate = {"!kotlin", "!taboolib.library.kotlin_@kotlin_version_escape@"}
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk8:@kotlin_version@",
        test = "!taboolib.library.kotlin_@kotlin_version_escape@.collections.jdk8.CollectionsJDK8Kt",
        relocate = {"!kotlin", "!taboolib.library.kotlin_@kotlin_version_escape@"}
)
@RuntimeDependency(value = "!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
public class RuntimeEnv {

    private boolean notify = false;

    public void setup() {
        loadDependency(RuntimeEnv.class);
    }

    public void inject(@NotNull Class<?> clazz) {
        loadAssets(clazz);
        loadDependency(clazz);
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
                        System.out.println("Loading assets, please wait...");
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

    public void loadDependency(@NotNull Class<?> clazz) {
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
                String test = dependency.test().startsWith("!") ? dependency.test().substring(1) : dependency.test();
                if (test.length() > 0 && ClassAppender.isExists(test)) {
                    continue;
                }
                Relocation relocation = null;
                if (dependency.relocate().length == 2) {
                    String pattern = dependency.relocate()[0].startsWith("!") ? dependency.relocate()[0].substring(1) : dependency.relocate()[0];
                    String relocatePattern = dependency.relocate()[1].startsWith("!") ? dependency.relocate()[1].substring(1) : dependency.relocate()[1];
                    relocation = new Relocation(pattern, relocatePattern);
                }
                try {
                    String[] args = dependency.value().startsWith("!") ? dependency.value().substring(1).split(":") : dependency.value().split(":");
                    DependencyDownloader downloader = new DependencyDownloader(relocation);
                    downloader.addRepository(new Repository(dependency.repository()));
                    // 解析依赖
                    File file1 = new File("libs", String.format("%s/%s/%s/%s-%s.pom", args[0].replace('.', '/'), args[1], args[2], args[1], args[2]));
                    File file2 = new File(file1.getPath() + ".sha1");
                    if (file1.exists() && file2.exists() && DependencyDownloader.readFileHash(file1).equals(DependencyDownloader.readFile(file2))) {
                        downloader.download(file1.toPath().toUri().toURL().openStream());
                    } else {
                        String pom = String.format("%s/%s/%s/%s/%s-%s.pom", dependency.repository(), args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
                        downloader.download(new URL(pom).openStream());
                    }
                    // 加载自身
                    downloader.injectClasspath(downloader.download(downloader.getRepositories(), new Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
