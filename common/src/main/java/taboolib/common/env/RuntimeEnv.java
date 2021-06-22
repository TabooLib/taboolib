package taboolib.common.env;

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
@Awake
public class RuntimeEnv {

    public RuntimeEnv() {
        for (Class<?> clazz : IOKt.getClasses()) {
            try {
                inject(clazz);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void inject(Class<?> clazz) throws IOException {
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
                String[] args = dependency.value().split(":");
                DependencyDownloader downloader = new DependencyDownloader();
                downloader.addRepository(new Repository(dependency.repository()));
                downloader.download(downloader.getRepositories(), new Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME));
                String pom = String.format("%s/%s/%s/%s/%s-%s.pom", dependency.repository(), args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
                downloader.download(new URL(pom).openStream());
            }
        }
    }
}
