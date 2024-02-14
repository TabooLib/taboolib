package taboolib.common.env;

import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import taboolib.common.ClassAppender;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.TabooLib;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 包含所有需要下载和注入依赖项到类路径的方法的类。
 *
 * @author Zach Deibert, sky
 * @since 1.0.0
 */
@SuppressWarnings("UnusedReturnValue")
public class DependencyDownloader extends AbstractXmlParser {

    /**
     * 已注入的依赖
     */
    private static final Map<Dependency, Set<ClassLoader>> injectedDependencies = new HashMap<>();

    /**
     * 已下载的依赖
     */
    private static final Set<Dependency> downloadedDependencies = new HashSet<>();

    /**
     * 仓库
     */
    private final Set<Repository> repositories = new HashSet<>();

    /**
     * 重定向规则
     */
    private final Set<JarRelocation> relocation = new HashSet<>();

    /**
     * 本地依赖目录
     */
    private final File baseDir;

    /**
     * 依赖范围
     */
    private DependencyScope[] dependencyScopes = {DependencyScope.RUNTIME, DependencyScope.COMPILE};

    /**
     * 忽略可选依赖
     */
    private boolean ignoreOptional = true;

    /**
     * 忽略异常
     */
    private boolean ignoreException = false;

    /**
     * 是否传递依赖
     */
    private boolean isTransitive = true;

    public DependencyDownloader(@Nullable File baseDir) {
        this.baseDir = baseDir;
    }

    public DependencyDownloader(@Nullable File baseDir, @Nullable List<JarRelocation> relocation) {
        this.baseDir = baseDir;
        if (relocation != null) {
            for (JarRelocation rel : relocation) {
                if (rel != null) {
                    this.relocation.add(rel);
                }
            }
        }
    }

    /**
     * 确保 {@link DependencyDownloader#baseDir} 存在
     */
    private void createBaseDir() {
        baseDir.mkdirs();
    }

    /**
     * 将一组依赖项注入到类路径中
     */
    public void injectClasspath(Set<Dependency> dependencies) throws Throwable {
        for (Dependency dep : dependencies) {
            // 如果已经注入过了，就跳过
            Set<ClassLoader> injectedDependencyClassLoaders = injectedDependencies.get(dep);
            if (injectedDependencyClassLoaders != null && injectedDependencyClassLoaders.contains(ClassAppender.getClassLoader())) {
                continue;
            }
            // 获取依赖项的文件
            File file = dep.findFile(baseDir, "jar");
            // 如果文件存在
            if (file.exists()) {
                // 提示信息
                PrimitiveIO.println("Loading library %s:%s:%s", dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
                // 如果没有重定向规则，直接注入
                if (relocation.isEmpty()) {
                    ClassLoader loader = ClassAppender.addPath(file.toPath(), PrimitiveSettings.IS_ISOLATED_MODE, dep.isExternal());
                    injectedDependencies.computeIfAbsent(dep, dependency -> new HashSet<>()).add(loader);
                } else {
                    // 获取重定向后的文件
                    String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    File rel = new File(file.getParentFile(), name + "_r2_" + Math.abs(relocation.hashCode()) + ".jar");
                    // 如果文件不存在或者文件大小为 0，就执行重定向逻辑
                    if (!rel.exists() || rel.length() == 0) {
                        try {
                            // 提示信息
                            PrimitiveIO.println("Relocating ...");
                            // 获取重定向规则
                            List<Relocation> rules = relocation.stream().map(JarRelocation::toRelocation).collect(Collectors.toList());
                            // 获取临时文件
                            File tempSourceFile = PrimitiveIO.copyFile(file, File.createTempFile(file.getName(), ".jar"));
                            // 运行
                            new JarRelocator(tempSourceFile, rel, rules).run();
                        } catch (IOException e) {
                            throw new IllegalStateException(String.format("Unable to relocate %s%n", dep), e);
                        }
                    }
                    // 注入重定向后的文件
                    ClassLoader loader = ClassAppender.addPath(rel.toPath(), PrimitiveSettings.IS_ISOLATED_MODE, dep.isExternal());
                    injectedDependencies.computeIfAbsent(dep, dependency -> new HashSet<>()).add(loader);
                }
            } else {
                try {
                    // 下载依赖项
                    loadDependency(repositories, dep);
                    // 重新注入
                    injectClasspath(Collections.singleton(dep));
                } catch (IOException e) {
                    TabooLib.setStopped(true);
                    throw new IllegalStateException("Unable to load dependency: " + dep, e);
                }
            }
        }
    }

    /**
     * 下载一个依赖项以及它的所有依赖项，并将它们存储在 {@link DependencyDownloader#baseDir} 中。
     */
    public Set<Dependency> loadDependency(Collection<Repository> repositories, Dependency dependency) throws IOException {
        // 未指定仓库
        if (repositories.isEmpty()) {
            throw new IllegalArgumentException("No repositories specified");
        }
        // 检查依赖版本
        dependency.checkVersion(repositories, baseDir);
        // 如果已经下载过了，就直接返回
        if (downloadedDependencies.contains(dependency)) {
            Set<Dependency> singleton = new HashSet<>();
            singleton.add(dependency);
            return singleton;
        }
        // 获取依赖项的 pom 文件和 jar 文件
        File pom = dependency.findFile(baseDir, "pom");
        File pom1 = new File(pom.getPath() + ".sha1");
        File jar = dependency.findFile(baseDir, "jar");
        File jar1 = new File(jar.getPath() + ".sha1");
        Set<Dependency> downloaded = new HashSet<>();
        downloaded.add(dependency);
        // 检查文件的完整性
        if (PrimitiveIO.validation(pom, pom1) && PrimitiveIO.validation(jar, jar1)) {
            // 加载依赖项
            downloadedDependencies.add(dependency);
            if (pom.exists()) {
                downloaded.addAll(loadDependencyFromInputStream(pom.toURI().toURL().openStream()));
            }
            return downloaded;
        }
        // 创建所在目录
        pom.getParentFile().mkdirs();
        // 下载文件
        IOException e = null;
        for (Repository repo : repositories) {
            try {
                repo.downloadFile(dependency, pom);
                repo.downloadFile(dependency, jar);
                e = null;
                break;
            } catch (Exception ex) {
                e = new IOException(String.format("Unable to find download for %s (%s)", dependency, repo.getUrl()), ex);
            }
        }
        // 如果存在异常，则抛出
        if (e != null) {
            throw e;
        }
        return downloaded;
    }

    /**
     * 下载一个依赖项列表以及它们的所有依赖项，并将它们存储在 {@link DependencyDownloader#baseDir} 中。
     */
    public Set<Dependency> loadDependency(List<Repository> repositories, List<Dependency> dependencies) throws IOException {
        createBaseDir();
        Set<Dependency> downloaded = new HashSet<>();
        for (Dependency dep : dependencies) {
            downloaded.addAll(loadDependency(repositories, dep));
        }
        return downloaded;
    }

    /**
     * 下载 pom 中指定的所有依赖项
     */
    public Set<Dependency> loadDependencyFromPom(Document pom, DependencyScope... scopes) throws IOException {
        List<Dependency> dependencies = new ArrayList<>();
        Set<DependencyScope> scopeSet = new HashSet<>(Arrays.asList(scopes));
        NodeList nodes = pom.getDocumentElement().getChildNodes();
        List<Repository> repos = new ArrayList<>(repositories);
        if (repos.isEmpty()) {
            repos.add(new Repository());
        }
        try {
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
                if (node.getNodeName().equals("repositories")) {
                    nodes = ((Element) node).getElementsByTagName("repository");
                    for (i = 0; i < nodes.getLength(); ++i) {
                        Element e = (Element) nodes.item(i);
                        repos.add(new Repository(e));
                    }
                    break;
                }
            }
        } catch (ParseException ex) {
            throw new IOException("Unable to parse repositories", ex);
        }
        if (isTransitive) {
            nodes = pom.getElementsByTagName("dependency");
            try {
                for (int i = 0; i < nodes.getLength(); ++i) {
                    // ignore optional
                    if (ignoreOptional && find("optional", (Element) nodes.item(i), "false").equals("true")) {
                        continue;
                    }
                    Dependency dep = new Dependency((Element) nodes.item(i));
                    if (scopeSet.contains(dep.getScope())) {
                        dependencies.add(dep);
                    }
                }
            } catch (ParseException ex) {
                if (!ignoreException) {
                    throw new IOException("Unable to parse dependencies", ex);
                }
            }
        }
        return loadDependency(repos, dependencies);
    }

    /**
     * 下载 pom 中指定的所有依赖项
     */
    public Set<Dependency> loadDependencyFromInputStream(InputStream pom) throws IOException {
        return loadDependencyFromInputStream(pom, dependencyScopes);
    }

    /**
     * 下载 pom 中指定的所有依赖项
     */
    public Set<Dependency> loadDependencyFromInputStream(InputStream pom, DependencyScope... scopes) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(pom);
            return loadDependencyFromPom(xml, scopes);
        } catch (ParserConfigurationException ex) {
            throw new IOException("Unable to load pom.xml parser", ex);
        } catch (SAXException ex) {
            throw new IOException("Unable to parse pom.xml", ex);
        }
    }

    public void addRepository(Repository repository) {
        repositories.add(repository);
    }

    public File getBaseDir() {
        return baseDir;
    }

    public DependencyScope[] getDependencyScopes() {
        return dependencyScopes;
    }

    public DependencyDownloader setDependencyScopes(DependencyScope[] dependencyScopes) {
        this.dependencyScopes = dependencyScopes;
        return this;
    }

    public Map<Dependency, Set<ClassLoader>> getInjectedDependencies() {
        return injectedDependencies;
    }

    public Set<Repository> getRepositories() {
        return repositories;
    }

    public boolean isIgnoreOptional() {
        return ignoreOptional;
    }

    public DependencyDownloader setIgnoreOptional(boolean ignoreOptional) {
        this.ignoreOptional = ignoreOptional;
        return this;
    }

    public DependencyDownloader setIgnoreException(boolean ignoreException) {
        this.ignoreException = ignoreException;
        return this;
    }

    public Set<JarRelocation> getRelocation() {
        return relocation;
    }

    public boolean isTransitive() {
        return isTransitive;
    }

    public void setTransitive(boolean transitive) {
        isTransitive = transitive;
    }
}
