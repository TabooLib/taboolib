package taboolib.library.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * YamlConstructor 类继承自 SafeConstructor，用于自定义 YAML 构造过程。
 */
public class YamlConstructor extends SafeConstructor {

    /**
     * 构造函数，初始化 YamlConstructor 实例。
     *
     * @param loaderOptions YAML 加载选项
     */
    public YamlConstructor(LoaderOptions loaderOptions) {
        super(loaderOptions);
        this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
    }

    /**
     * 重写 flattenMapping 方法，用于扁平化映射节点。
     *
     * @param node 要扁平化的映射节点
     */
    @Override
    public void flattenMapping(@NotNull final MappingNode node) {
        super.flattenMapping(node);
    }

    /**
     * 构造给定节点的对象。
     *
     * @param node 要构造的节点
     * @return 构造的对象，可能为 null
     */
    @Nullable
    public Object construct(@NotNull Node node) {
        return constructObject(node);
    }

    /**
     * ConstructCustomObject 内部类，用于自定义对象的构造。
     */
    class ConstructCustomObject extends ConstructYamlMap {

        /**
         * 构造给定节点的对象。
         *
         * @param node 要构造的节点
         * @return 构造的对象，可能为 null
         * @throws YAMLException 如果遇到意外的引用映射结构
         */
        @Nullable
        @Override
        public Object construct(@NotNull Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
            }
            return super.construct(node);
        }

        /**
         * 执行对象构造的第二步。
         *
         * @param node 要构造的节点
         * @param object 已构造的对象
         * @throws YAMLException 如果遇到意外的引用映射结构
         */
        @Override
        public void construct2ndStep(@NotNull Node node, @NotNull Object object) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
        }
    }
}