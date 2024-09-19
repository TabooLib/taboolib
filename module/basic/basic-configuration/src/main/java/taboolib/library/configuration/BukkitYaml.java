package taboolib.library.configuration;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentEventsCollector;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.serializer.Serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * BukkitYaml 类扩展了 Yaml 类，提供了对 YAML 文件的特定处理功能。
 * 这个类主要用于处理 Bukkit 配置文件中的注释和序列化。
 */
public class BukkitYaml extends Yaml {

    /** 用于存储 Emitter 类中的 events 字段 */
    private static final Field events;
    /** 用于存储 Emitter 类中的 blockCommentsCollector 字段 */
    private static final Field blockCommentsCollector;
    /** 用于存储 Emitter 类中的 inlineCommentsCollector 字段 */
    private static final Field inlineCommentsCollector;

    /**
     * 获取 Emitter 类中指定名称的字段。
     *
     * @param name 字段名称
     * @return 返回对应的 Field 对象，如果获取失败则返回 null
     */
    private static Field getEmitterField(String name) {
        Field field = null;
        try {
            field = Emitter.class.getDeclaredField(name);
            field.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            // 忽略异常，作为一个安全的回退机制
        }
        return field;
    }

    // 静态初始化块，用于初始化静态字段
    static {
        events = getEmitterField("events");
        blockCommentsCollector = getEmitterField("blockCommentsCollector");
        inlineCommentsCollector = getEmitterField("inlineCommentsCollector");
    }

    /**
     * 构造函数，初始化 BukkitYaml 实例。
     *
     * @param constructor YAML 构造器
     * @param representer YAML 表示器
     * @param dumperOptions 转储选项
     * @param loadingConfig 加载配置
     */
    public BukkitYaml(@NotNull BaseConstructor constructor, @NotNull Representer representer, @NotNull DumperOptions dumperOptions, @NotNull LoaderOptions loadingConfig) {
        super(constructor, representer, dumperOptions, loadingConfig);
    }

    /**
     * 重写序列化方法，用于处理 YAML 节点的序列化。
     *
     * @param node 要序列化的 YAML 节点
     * @param output 输出写入器
     */
    @Override
    public void serialize(@NotNull Node node, @NotNull Writer output) {
        Emitter emitter = new Emitter(output, dumperOptions);
        if (events != null && blockCommentsCollector != null && inlineCommentsCollector != null) {
            Queue<Event> newEvents = new ArrayDeque<>(100);
            try {
                events.set(emitter, newEvents);
                blockCommentsCollector.set(emitter, new CommentEventsCollector(newEvents, CommentType.BLANK_LINE, CommentType.BLOCK));
                inlineCommentsCollector.set(emitter, new CommentEventsCollector(newEvents, CommentType.IN_LINE));
            } catch (ReflectiveOperationException ex) {
                // 不要忽略这个异常，因为我们可能处于不一致的状态
                throw new RuntimeException("无法更新 Yaml 事件队列", ex);
            }
        }
        Serializer serializer = new Serializer(emitter, resolver, dumperOptions, null);
        try {
            serializer.open();
            serializer.serialize(node);
            serializer.close();
        } catch (IOException ex) {
            throw new YAMLException(ex);
        }
    }
}