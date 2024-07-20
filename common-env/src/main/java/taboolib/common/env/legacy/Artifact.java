package taboolib.common.env.legacy;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TabooLib
 * taboolib.common.env.legacy.Artifact
 *
 * @author 坏黑
 * @since 2024/7/20 22:24
 */
public class Artifact {

    private static final Pattern COORDINATE_PATTERN = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String extension;

    public Artifact(String coords) {
        this(coords, Collections.emptyMap());
    }

    public Artifact(String coords, Map<String, String> properties) {
        Matcher m = COORDINATE_PATTERN.matcher(coords);
        if (!m.matches()) {
            throw new IllegalArgumentException("Bad artifact coordinates " + coords + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>");
        } else {
            this.groupId = m.group(1);
            this.artifactId = m.group(2);
            this.extension = get(m.group(4), "jar");
            this.classifier = get(m.group(6), "");
            this.version = m.group(7);
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getExtension() {
        return extension;
    }

    static String get(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append(this.getGroupId());
        buffer.append(':').append(this.getArtifactId());
        buffer.append(':').append(this.getExtension());
        if (!this.getClassifier().isEmpty()) {
            buffer.append(':').append(this.getClassifier());
        }
        buffer.append(':').append(this.getVersion());
        return buffer.toString();
    }
}
