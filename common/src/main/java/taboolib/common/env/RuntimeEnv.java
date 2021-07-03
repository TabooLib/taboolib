package taboolib.common.env;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * TabooLib
 * taboolib.common.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
public class RuntimeEnv {

    public void inject(@NotNull Class<?> clazz) {
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
                    file = new File("assets/" + resource.value().substring(resource.value().lastIndexOf("/")));
                } else {
                    file = new File("assets/" + resource.name());
                }
                if (file.exists() && DependencyDownloader.readFileHash(file).equals(resource.hash())) {
                    continue;
                }
                try {
                    System.out.println("Loading Assets " + file.getName());
                    Repository.downloadToFile(new URL(resource.value()), file);
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
                if (dependency.test().length() > 0 && ClassAppender.isExists(dependency.test())) {
                    continue;
                }
                try {
                    String[] args = dependency.value().split(":");
                    DependencyDownloader downloader = new DependencyDownloader();
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
