package taboolib.common.env.aether;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.ClassAppender;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.env.DependencyScope;
import taboolib.common.env.JarRelocation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static taboolib.common.PrimitiveIO.t;

/**
 * @author md_5, sky
 * @since 2024/7/20 20:31
 */
@SuppressWarnings("deprecation")

public class AetherResolver {

    private static final Map<String, AetherResolver> resolverMap = Maps.newConcurrentMap();
    private static final Set<String> injectedDependencies = Sets.newConcurrentHashSet();

    private final RepositorySystem repository;
    private final DefaultRepositorySystemSession session;
    private final List<RemoteRepository> repositories;

    public AetherResolver(String repo) {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        this.repository = locator.getService(RepositorySystem.class);
        this.session = MavenRepositorySystemUtils.newSession();
        this.session.setChecksumPolicy("fail");
        this.session.setLocalRepositoryManager(this.repository.newLocalRepositoryManager(this.session, new LocalRepository("libraries")));
        this.session.setTransferListener(new AbstractTransferListener() {
            @Override
            public void transferStarted(@NotNull TransferEvent event) {
                PrimitiveIO.println(t("正在下载 {0}", "Downloading {0}"), event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
            }
        });
        this.session.setReadOnly();
        this.repositories = this.repository.newResolutionRepositories(this.session, Collections.singletonList(
                new RemoteRepository.Builder("central", "default", repo).build()
        ));
    }

    public List<File> resolve(@NotNull String library, List<DependencyScope> scope, boolean isTransitive, boolean ignoreOptional) {
        Dependency dependency = new Dependency(new DefaultArtifact(library), null);
        DependencyResult result;
        try {
            DependencyRequest dependencyRequest = getDependencyRequest(dependency, scope, isTransitive, ignoreOptional);
            result = this.repository.resolveDependencies(this.session, dependencyRequest);
        } catch (DependencyResolutionException ex) {
            throw new RuntimeException("Error resolving libraries", ex);
        }
        return result.getArtifactResults().stream().map(it -> it.getArtifact().getFile()).collect(Collectors.toList());
    }

    private @NotNull DependencyRequest getDependencyRequest(Dependency dependency, List<DependencyScope> scope, boolean isTransitive, boolean ignoreOptional) {
        return new DependencyRequest(new CollectRequest(dependency, null, repositories), new DependencyFilter() {
            boolean self = true;

            @Override
            public boolean accept(DependencyNode node, List<DependencyNode> parents) {
                // 忽略可选
                if (ignoreOptional && node.getDependency().isOptional()) return false;
                // 依赖传递
                if (isTransitive) return true;
                if (self) {
                    self = false;
                    return true;
                }
                return false;
            }
        });
    }

    public static AetherResolver of(@NotNull String repository) {
        return resolverMap.computeIfAbsent(repository, AetherResolver::new);
    }

    public static @Nullable ClassLoader inject(@NotNull File file, @Nullable List<JarRelocation> relocation, boolean isExternal) throws Throwable {
        // 避免重复加载多个依赖
        if (injectedDependencies.contains(file.getPath())) return null;
        else injectedDependencies.add(file.getParent());
        // 如果没有重定向规则，直接注入
        if (relocation == null || relocation.isEmpty()) {
            return ClassAppender.addPath(file.toPath(), PrimitiveSettings.IS_ISOLATED_MODE, isExternal);
        } else {
            // 获取重定向后的文件
            String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
            File rel = new File(file.getParentFile(), name + "_r2_" + Math.abs(relocation.hashCode()) + ".jar");
            // 如果文件不存在或者文件大小为 0，就执行重定向逻辑
            if (!rel.exists() || rel.length() == 0) {
                try {
                    // 获取重定向规则
                    List<Relocation> rules = relocation.stream().map(JarRelocation::toRelocation).collect(Collectors.toList());
                    // 获取临时文件
                    File tempSourceFile = PrimitiveIO.copyFile(file, File.createTempFile(file.getName(), ".jar"));
                    // 运行
                    new JarRelocator(tempSourceFile, rel, rules).run();
                } catch (IOException e) {
                    throw new IllegalStateException(String.format("Unable to relocate %s%n", file), e);
                }
            }
            // 注入重定向后的文件
            return ClassAppender.addPath(rel.toPath(), PrimitiveSettings.IS_ISOLATED_MODE, isExternal);
        }
    }
}
