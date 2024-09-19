package taboolib.library.configuration;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

/**
 * YamlRepresenter 类扩展了 Representer 类，用于自定义 YAML 表示。
 */
public class YamlRepresenter extends Representer {

    /**
     * 构造函数，初始化 YamlRepresenter。
     *
     * @param options DumperOptions 实例，用于配置 YAML 转储选项。
     */
    public YamlRepresenter(DumperOptions options) {
        super(options);
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
    }

    /**
     * 内部类 RepresentConfigurationSection，用于表示 ConfigurationSection 对象。
     */
    class RepresentConfigurationSection extends RepresentMap {

        /**
         * 重写 representData 方法，用于将 ConfigurationSection 对象转换为 YAML 节点。
         *
         * @param data 要表示的对象，应为 ConfigurationSection 类型。
         * @return 表示给定数据的 YAML 节点。
         */
        @Override
        public Node representData(Object data) {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }
    }
}
