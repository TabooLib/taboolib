package taboolib.common.env;

import me.lucko.jarrelocator.Relocation;

/**
 * TabooLib
 * taboolib.common.env.JarRelocation
 *
 * @author 坏黑
 * @since 2022/5/17 00:45
 */
public class JarRelocation {

    private final String pattern;
    private final String relocatedPattern;

    public JarRelocation(String pattern, String relocatedPattern) {
        this.pattern = pattern;
        this.relocatedPattern = relocatedPattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String getRelocatedPattern() {
        return relocatedPattern;
    }

    public Relocation toRelocation() {
        return new Relocation(pattern, relocatedPattern);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JarRelocation)) return false;
        JarRelocation that = (JarRelocation) o;
        if (getPattern() != null ? !getPattern().equals(that.getPattern()) : that.getPattern() != null) return false;
        return getRelocatedPattern() != null ? getRelocatedPattern().equals(that.getRelocatedPattern()) : that.getRelocatedPattern() == null;
    }

    @Override
    public int hashCode() {
        int result = getPattern() != null ? getPattern().hashCode() : 0;
        result = 31 * result + (getRelocatedPattern() != null ? getRelocatedPattern().hashCode() : 0);
        return result;
    }
}