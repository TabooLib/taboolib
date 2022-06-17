package taboolib.common.env;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a version parsed into its components
 *
 * @author Zach Deibert
 * @since 1.0.0
 */
public class DependencyVersion implements Comparable<DependencyVersion> {

    /**
     * A list of the components of the version
     *
     * @since 1.0.0
     */
    private final List<Integer> parts;

    /**
     * The version as a string
     *
     * @since 1.0.0
     */
    private final String version;

    public int compareTo(DependencyVersion o) {
        Iterator<Integer> us = parts.iterator();
        Iterator<Integer> them = o.parts.iterator();
        while (us.hasNext() && them.hasNext()) {
            int diff = us.next().compareTo(them.next());
            if (diff != 0) {
                return diff;
            }
        }
        return us.hasNext() ? 1 : them.hasNext() ? -1 : 0;
    }

    @Override
    public String toString() {
        return version;
    }

    /**
     * Creates a new version
     *
     * @param version The version string to parse
     * @since 1.0.0
     */
    public DependencyVersion(String version) {
        parts = new ArrayList<>();
        for (String part : version.split("[^0-9]")) {
            if (!part.isEmpty()) {
                parts.add(Integer.parseInt(part));
            }
        }
        this.version = version;
    }
}
