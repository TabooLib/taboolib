package taboolib.common.env.legacy;

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
     * 版本的组件列表
     */
    private final List<Integer> parts;

    /**
     * 作为字符串的版本
     */
    private final String version;

    public DependencyVersion(String version) {
        parts = new ArrayList<>();
        for (String part : version.split("[^0-9]")) {
            if (!part.isEmpty()) {
                parts.add(Integer.parseInt(part));
            }
        }
        this.version = version;
    }

    @Override
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
}
