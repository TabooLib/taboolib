package taboolib.common.env;

import org.tabooproject.reflex.LazyEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParsedDependency {

    /**
     * 依赖地址，格式为：
     * <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
     */
    private final String value;

    /**
     * 测试类
     * <p>
     * <code>
     * test = "!org.bukkit.Bukkit" // 前面带个感叹号避免在编译时重定向
     * </code>
     */
    private final String test;

    /**
     * 仓库地址，留空默认使用 <a href="https://maven.aliyun.com/repository/central">阿里云中央仓库</a>
     */
    private final String repository;

    /**
     * 是否进行依赖传递
     */
    private final boolean transitive;

    /**
     * 忽略可选依赖
     */
    private final boolean ignoreOptional;

    /**
     * 忽略加载异常
     */
    private final boolean ignoreException;

    /**
     * 依赖范围
     */
    private final List<DependencyScope> scopes;

    /**
     * 依赖重定向
     * <p>
     * <code>
     * relocate = ["!taboolib.", "!taboolib610."] // 同 test 参数
     * </code>
     */
    private final List<String> relocate;

    /**
     * 是否外部库（不会被扫到）
     */
    private final boolean external;

    @SuppressWarnings("unchecked")
    public ParsedDependency(Map<String, Object> map) {
        this.value = (String) map.get("value");
        this.test = (String) map.getOrDefault("test", "");
        this.repository = (String) map.getOrDefault("repository", "");
        this.transitive = (boolean) map.getOrDefault("transitive", true);
        this.ignoreOptional = (boolean) map.getOrDefault("ignoreOptional", true);
        this.ignoreException = (boolean) map.getOrDefault("ignoreException", false);
        this.relocate = (List<String>) map.getOrDefault("relocate", new ArrayList<>());
        this.external = (boolean) map.getOrDefault("external", true);
        this.scopes = new ArrayList<>();
        List<LazyEnum> scopesEnums = (List<LazyEnum>) map.getOrDefault("scopes", new ArrayList<>());
        scopesEnums.forEach(it -> this.scopes.add((DependencyScope) it.getInstance()));
    }

    public String value() {
        return value;
    }

    public String test() {
        return test;
    }

    public String repository() {
        return repository;
    }

    public boolean transitive() {
        return transitive;
    }

    public boolean ignoreOptional() {
        return ignoreOptional;
    }

    public boolean ignoreException() {
        return ignoreException;
    }

    public List<DependencyScope> scopes() {
        return scopes;
    }

    public List<String> relocate() {
        return relocate;
    }

    public boolean external() {
        return external;
    }

    @Override
    public String toString() {
        return "ParsedDependency{" +
                "value='" + value + '\'' +
                ", test='" + test + '\'' +
                ", repository='" + repository + '\'' +
                ", transitive=" + transitive +
                ", ignoreOptional=" + ignoreOptional +
                ", ignoreException=" + ignoreException +
                ", scopes=" + scopes +
                ", relocate=" + relocate +
                ", external=" + external +
                '}';
    }
}
