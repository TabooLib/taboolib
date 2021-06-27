package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.inject.Injector;
import taboolib.common.io.IOKt;
import taboolib.common.platform.Awake;

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
                    String pom = String.format("%s/%s/%s/%s/%s-%s.pom", dependency.repository(), args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
                    downloader.download(new URL(pom).openStream());
                    downloader.injectClasspath(downloader.download(downloader.getRepositories(), new Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
