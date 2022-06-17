package taboolib.common.env;

import org.w3c.dom.Element;

import java.io.File;
import java.text.ParseException;
import java.util.Objects;

/**
 * Represents a dependency that needs to be downloaded and injected into the
 * classpath
 *
 * @author Zach Deibert, sky
 * @since 1.0.0
 */
public class Dependency extends AbstractXmlParser {

    /**
     * A placeholder string for when the version has not been specified
     *
     * @since 1.0.0
     */
    private static final String LATEST_VERSION = "latest";

    /**
     * The ID of the group for this dependency
     *
     * @since 1.0.0
     */
    private final String groupId;

    /**
     * The ID of the artifact for this dependency
     *
     * @since 1.0.0
     */
    private final String artifactId;

    /**
     * The version of the artifact to download, or
     * {@link Dependency#LATEST_VERSION} if it is not specified in the pom
     *
     * @since 1.0.0
     */
    private String version;

    /**
     * The scope of the dependency
     *
     * @since 1.0.0
     */
    private final DependencyScope scope;

    /**
     * Gets the ID of the group for this dependency
     *
     * @return The group ID
     * @since 1.0.0
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Gets the ID of the artifact for this dependency
     *
     * @return The artifact ID
     * @since 1.0.0
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Gets the version of the artifact to download
     *
     * @return The version, or <code>null</code> if the latest version should be
     * downloaded and the latest version has not been determined yet
     * @since 1.0.0
     */
    public String getVersion() {
        return version.equals(LATEST_VERSION) ? null : version;
    }

    /**
     * Sets the latest version of this dependency
     *
     * @param version The latest version
     * @throws IllegalStateException If this dependency has a specific version already
     * @since 1.0.0
     */
    public void setVersion(String version) {
        if (!this.version.equals(LATEST_VERSION)) {
            throw new IllegalStateException("Version is already resolved");
        } else if (version.equals(LATEST_VERSION)) {
            throw new IllegalArgumentException("Cannot set version to the latest");
        } else {
            this.version = version;
        }
    }

    /**
     * Gets a list of all of the versions of this artifact that are currently
     * downloaded on this computer
     *
     * @param dir The directory to store downloaded artifacts in
     * @return An array of the versions that are already downloaded
     * @since 1.0.0
     */
    public DependencyVersion[] getInstalledVersions(File dir) {
        for (String part : getGroupId().split("\\.")) {
            dir = new File(dir, part);
        }
        dir = new File(dir, getArtifactId());
        String[] list = dir.list();
        if (list == null) {
            return new DependencyVersion[0];
        }
        DependencyVersion[] versions = new DependencyVersion[list.length];
        for (int i = 0; i < list.length; ++i) {
            versions[i] = new DependencyVersion(list[i]);
        }
        return versions;
    }

    /**
     * Gets the scope of this dependency
     *
     * @return The scope
     * @since 1.0.0
     */
    public DependencyScope getScope() {
        return scope;
    }

    /**
     * Gets the file that the downloaded artifact should be stored in
     *
     * @param dir The directory to store downloaded artifacts in
     * @param ext The file extension to download (should be either
     *            <code>"jar"</code> or <code>"pom"</code>
     * @return The file to download into
     * @since 1.0.0
     */
    public File getFile(File dir, String ext) {
        for (String part : getGroupId().split("\\.")) {
            dir = new File(dir, part);
        }
        dir = new File(dir, getArtifactId());
        dir = new File(dir, getVersion());
        dir = new File(dir, String.format("%s-%s.%s", getArtifactId(), getVersion(), ext));
        return dir;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dependency)) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(getGroupId(), that.getGroupId()) && Objects.equals(getArtifactId(), that.getArtifactId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupId(), getArtifactId());
    }

    /**
     * Creates a new dependency
     *
     * @param groupId    The group ID
     * @param artifactId The artifact ID
     * @param version    The version to download
     * @param scope      The scope
     * @since 1.0.0
     */
    public Dependency(String groupId, String artifactId, String version, DependencyScope scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version.contains("$") || version.contains("[") || version.contains("(") ? LATEST_VERSION : version;
        this.scope = scope;
    }

    /**
     * Creates a new dependency from the specified element in the pom
     *
     * @param node The element to create the dependency from
     * @throws ParseException If the xml could not be parsed
     * @since 1.0.0
     */
    public Dependency(Element node) throws ParseException {
        this(find("groupId", node), find("artifactId", node), find("version", node, LATEST_VERSION), DependencyScope.valueOf(find("scope", node, "runtime").toUpperCase()));
    }
}
