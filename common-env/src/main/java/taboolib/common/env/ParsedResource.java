package taboolib.common.env;

import java.util.Map;

public class ParsedResource {

    /**
     * 资源地址
     */
    private final String value;

    /**
     * 实际资源的哈希值
     * 采用 SHA-1 算法
     */
    private final String hash;

    /**
     * 资源名称
     * 留空使用哈希值作为名称
     */
    private final String name;

    /**
     * 标签（标识用）
     */
    private final String tag;

    /**
     * 是否为压缩文件，启用后从 value() + ".zip" 下载文件，
     * 无论是否启用，哈希值判断都依据 ".zip" 内的实际资源，而非 ".zip" 本身
     */
    private final boolean zip;

    public ParsedResource(Map<String, Object> annotation) {
        this.value = (String) annotation.get("value");
        this.hash = (String) annotation.get("hash");
        this.name = (String) annotation.get("name");
        this.tag = (String) annotation.getOrDefault("tag", "");
        this.zip = (boolean) annotation.getOrDefault("zip", false);
    }

    public String value() {
        return value;
    }

    public String hash() {
        return hash;
    }

    public String name() {
        return name;
    }

    public String tag() {
        return tag;
    }

    public Boolean zip() {
        return zip;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "value='" + value + '\'' +
                ", hash='" + hash + '\'' +
                ", name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", zip=" + zip +
                '}';
    }
}
