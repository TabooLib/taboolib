package taboolib.common.env;

import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import taboolib.common.TabooLibCommon;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The class that contains all of the methods needed for downloading and
 * injecting dependencies into the classpath.
 *
 * @author Zach Deibert, sky
 * @since 1.0.0
 */
@SuppressWarnings("UnusedReturnValue")
public class DependencyDownloader extends AbstractXmlParser {

    /**
     * A set of all of the dependencies that have already been injected into the
     * classpath, so they should not be reinjected (to prevent cyclic
     * dependencies from freezing the code in a loop)
     *
     * @since 1.0.0
     */
    private static final Set<Dependency> injectedDependencies = new HashSet<>();
    private static final Set<Dependency> downloadedDependencies = new HashSet<>();

    private final Set<Repository> repositories = new HashSet<>();

    private final Set<JarRelocation> relocation = new HashSet<>();

    /**
     * The directory to download and store artifacts in
     *
     * @since 1.0.0
     */
    private File baseDir = new File("libs");

    /**
     * The scopes to download dependencies for by default
     *
     * @since 1.0.0
     */
    private DependencyScope[] dependencyScopes = {DependencyScope.RUNTIME, DependencyScope.COMPILE};

    /**
     * If debugging information should be logged to {@link System#out}
     *
     * @since 1.0.0
     */
    private boolean isDebugMode = true;

    private boolean ignoreOptional = true;

    private boolean ignoreException = false;

    private boolean isTransitive = true;

    public DependencyDownloader() {
    }

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
     * Makes sure that the {@link DependencyDownloader#baseDir} exists
     *
     * @since 1.0.0
     */
    private void createBaseDir() {
        baseDir.mkdirs();
    }

    /**
     * Injects a set of dependencies into the classpath
     *
     * @param dependencies The dependencies to inject
     * @since 1.0.0
     */
    public void injectClasspath(Set<Dependency> dependencies) {
        for (Dependency dep : dependencies) {
            if (injectedDependencies.contains(dep)) {
                continue;
            }
            File file = dep.getFile(baseDir, "jar");
            if (file.exists()) {
                TabooLibCommon.print(String.format("Loading library %s:%s:%s", dep.getGroupId(), dep.getArtifactId(), dep.getVersion()));
                if (relocation.isEmpty()) {
                    ClassAppender.addPath(file.toPath());
                } else {
                    File rel = new File(file.getPath() + "-" + relocation.hashCode() + ".jar");
                    if (!rel.exists() || rel.length() == 0) {
                        try {
                            TabooLibCommon.print("Relocating ...");
                            List<Relocation> relocations = relocation.stream().map(JarRelocation::toRelocation).collect(Collectors.toList());
                            new JarRelocator(copyFile(file, File.createTempFile(file.getName(), ".jar")), rel, relocations).run();
                        } catch (IOException e) {
                            throw new IllegalStateException(String.format("Unable to relocate %s%n", dep), e);
                        }
                    }
                    ClassAppender.addPath(rel.toPath());
                }
                injectedDependencies.add(dep);
            } else {
                try {
                    loadDependency(repositories, dep);
                    injectClasspath(Collections.singleton(dep));
                } catch (IOException e) {
                    TabooLibCommon.setStopped(true);
                    throw new IllegalStateException("Unable to load dependency: " + dep, e);
                }
            }
        }
    }

    /**
     * Downloads a dependency along with all of its dependencies and stores them
     * in the {@link DependencyDownloader#baseDir}.
     *
     * @param repositories The list of repositories to try to download from
     * @param dependency   The dependency to download
     * @return The set of all dependencies that were downloaded
     * @throws IOException If an I/O error has occurred
     * @since 1.0.0
     */
    public Set<Dependency> loadDependency(Collection<Repository> repositories, Dependency dependency) throws IOException {
        if (dependency.getVersion() == null) {
            IOException e = null;
            for (Repository repo : repositories) {
                try {
                    repo.setVersion(dependency);
                    e = null;
                    break;
                } catch (IOException ex) {
                    if (e == null) {
                        e = new IOException(String.format("Unable to find latest version of %s", dependency));
                    }
                    e.addSuppressed(ex);
                }
            }
            if (e != null) {
                DependencyVersion max = null;
                for (DependencyVersion ver : dependency.getInstalledVersions(baseDir)) {
                    if (max == null || ver.compareTo(max) > 0) {
                        max = ver;
                    }
                }
                if (max == null) {
                    throw e;
                } else {
                    dependency.setVersion(max.toString());
                }
            }
        }
        if (downloadedDependencies.contains(dependency)) {
            Set<Dependency> singleton = new HashSet<>();
            singleton.add(dependency);
            return singleton;
        }
        File pom = dependency.getFile(baseDir, "pom");
        File pom1 = new File(pom.getPath() + ".sha1");
        File jar = dependency.getFile(baseDir, "jar");
        File jar1 = new File(jar.getPath() + ".sha1");
        Set<Dependency> downloaded = new HashSet<>();
        downloaded.add(dependency);
        if (pom.exists() && pom1.exists() && jar.exists() && jar1.exists() && readFile(pom1).startsWith(readFileHash(pom)) && readFile(jar1).startsWith(readFileHash(jar))) {
            downloadedDependencies.add(dependency);
            if (pom.exists()) {
                downloaded.addAll(loadDependencyFromInputStream(pom.toURI().toURL().openStream()));
            }
            return downloaded;
        }
        pom.getParentFile().mkdirs();
        IOException e = null;
        for (Repository repo : repositories) {
            try {
                repo.downloadToFile(dependency, pom);
                repo.downloadToFile(dependency, new File(pom.getPath() + ".sha1"));
                try {
                    repo.downloadToFile(dependency, jar);
                    repo.downloadToFile(dependency, new File(jar.getPath() + ".sha1"));
                } catch (IOException exception) {
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document xml = builder.parse(pom);
                        try {
                            if (find("packaging", xml.getDocumentElement(), "pom").equals("jar")) {
                                throw exception;
                            }
                        } catch (ParseException ex) {
                            ex.addSuppressed(exception);
                            throw new IOException("Unable to find packaging information in pom.xml", ex);
                        }
                    } catch (ParserConfigurationException ex) {
                        ex.addSuppressed(exception);
                        throw new IOException("Unable to load pom.xml parser", ex);
                    } catch (SAXException ex) {
                        ex.addSuppressed(exception);
                        throw new IOException("Unable to parse pom.xml", ex);
                    } catch (IOException ex) {
                        if (!ex.equals(exception)) {
                            ex.addSuppressed(exception);
                        }
                        throw ex;
                    }
                }
                if (pom.exists()) {
                    downloaded.addAll(loadDependencyFromInputStream(pom.toURI().toURL().openStream()));
                }
                e = null;
                break;
            } catch (IOException ex) {
                if (e == null) {
                    e = new IOException(String.format("Unable to find download for %s (%s)", dependency, repo.getUrl()));
                }
                e.addSuppressed(ex);
            }
        }
        if (e != null) {
            throw e;
        }
        return downloaded;
    }

    /**
     * Downloads a list of dependencies along with all of their dependencies and
     * stores them in the {@link DependencyDownloader#baseDir}.
     *
     * @param repositories The list of repositories to try to download from
     * @param dependencies The list of dependencies to download
     * @return The set of all dependencies that were downloaded
     * @throws IOException If an I/O error has occurred
     * @since 1.0.0
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
     * Downloads all of the dependencies specified in the pom
     *
     * @param pom    The parsed pom file
     * @param scopes The scopes to download for
     * @return The set of all dependencies that were downloaded
     * @throws IOException If an I/O error has occurred
     * @since 1.0.0
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
     * Downloads all of the dependencies specified in the pom for the default
     * scopes
     *
     * @param pom The parsed pom file
     * @return The set of all dependencies that were downloaded
     * @throws IOException If an I/O error has occurred
     * @see DependencyDownloader#dependencyScopes
     * @since 1.0.0
     */
    public Set<Dependency> loadDependencyFromPom(Document pom) throws IOException {
        return loadDependencyFromPom(pom, dependencyScopes);
    }

    /**
     * Downloads all of the dependencies specified in the pom for the default
     * scopes
     *
     * @param pom The stream containing the pom file
     * @return The set of all dependencies that were downloaded
     * @throws IOException If an I/O error has occurred
     * @see DependencyDownloader#dependencyScopes
     * @since 1.0.0
     */
    public Set<Dependency> loadDependencyFromInputStream(InputStream pom) throws IOException {
        return loadDependencyFromInputStream(pom, dependencyScopes);
    }

    /**
     * Downloads all of the dependencies specified in the pom
     *
     * @param pom    The stream containing the pom file
     * @param scopes The scopes to download for
     * @return The set of all dependencies that were downloaded
     * @throws IOException If an I/O error has occurred
     * @since 1.0.0
     */
    public Set<Dependency> loadDependencyFromInputStream(InputStream pom, DependencyScope... scopes) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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

    public DependencyDownloader setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public DependencyScope[] getDependencyScopes() {
        return dependencyScopes;
    }

    public DependencyDownloader setDependencyScopes(DependencyScope[] dependencyScopes) {
        this.dependencyScopes = dependencyScopes;
        return this;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public DependencyDownloader setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
        return this;
    }

    public Set<Dependency> getInjectedDependencies() {
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

    @NotNull
    public static String readFileHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-1");
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                byte[] buffer = new byte[1024];
                int total;
                while ((total = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, total);
                }
            }
            return getHash(digest);
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    private static String getHash(MessageDigest digest) {
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @NotNull
    public static String readFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return readFully(fileInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    public static String readFully(InputStream inputStream, Charset charset) throws IOException {
        return new String(readFully(inputStream), charset);
    }

    public static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    private static File copyFile(File file1, File file2) {
        try (FileInputStream fileIn = new FileInputStream(file1); FileOutputStream fileOut = new FileOutputStream(file2); FileChannel channelIn = fileIn.getChannel(); FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
        return file2;
    }
}
