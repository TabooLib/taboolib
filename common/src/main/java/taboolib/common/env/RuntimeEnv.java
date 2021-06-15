package taboolib.common.env;

import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.dependency.StandardDependency;
import dev.vankka.dependencydownload.repository.StandardRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Flow;

/**
 * TabooLib
 * taboolib.common.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
public class RuntimeEnv {

    private final String name;
    private String checkClass;
    private final List<StandardDependency> dependency = new ArrayList<>();
    private final List<String> repository = new ArrayList<>();

    public static RuntimeEnv setup(String name) {
        return new RuntimeEnv(name).repository("https://maven.aliyun.com/repository/central", "https://repo1.maven.org/maven2");
    }

    RuntimeEnv(String name) {
        this.name = name;
    }

    public RuntimeEnv check(String checkClass) {
        this.checkClass = checkClass;
        return this;
    }

    public RuntimeEnv repository(String... repository) {
        this.repository.clear();
        this.repository.addAll(Arrays.asList(repository));
        return this;
    }

    public RuntimeEnv add(String groupId, String artifactId, String version, String hash, String hashingAlgorithm) {
        dependency.add(new StandardDependency(groupId, artifactId, version, hash, hashingAlgorithm));
        return this;
    }

    public void run() {
        if (checkClass == null || !ClassAppender.INSTANCE.isExists(checkClass)) {
            System.out.println("[TabooLib] Loading " + name + " runtime environment.");
            DependencyManager manager = new DependencyManager(new File("libs").toPath());
            dependency.forEach(manager::addDependency);
            long time = System.currentTimeMillis();
            for (String it : repository) {
                try {
                    manager.downloadAll(Runnable::run, Collections.singletonList(new StandardRepository(it))).join();
                    manager.loadAll(Runnable::run, ClassAppender.INSTANCE).join();
                    System.out.println("[TabooLib] Loaded (" + (System.currentTimeMillis() - time) + "ms)!");
                    return;
                } catch (NullPointerException ignored) {
                }
            }
            System.out.println("[TabooLib] Loading failed, check your internet connection and try again.");
        }
    }
}
