package taboolib.library.configuration;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentEventsCollector;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.serializer.Serializer;

import java.io.IOException;
import java.io.Reader;
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
    private static final Field emitterEvents;
    /** 用于存储 Emitter 类中的 blockCommentsCollector 字段 */
    private static final Field emitterBlockCommentsCollector;
    /** 用于存储 Emitter 类中的 inlineCommentsCollector 字段 */
    private static final Field emitterInlineCommentsCollector;

    /**
     * 用于存储 Composer 类中的 blockCommentsCollector 字段。
     *
     * SnakeYAML 的 Composer.composeNode() 在 blockCommentsCollector.collectEvents() 后
     * 直接将 parser.peekEvent() 强转为 NodeEvent，但 blockCommentsCollector 只处理
     * BLANK_LINE 和 BLOCK 类型的注释事件。当 ScannerImpl 将独占一行的注释误判为 IN_LINE
     * 类型时（因其与前一个行内注释处于同一列），该注释事件不会被收集，导致 ClassCastException。
     *
     * 修复方式：将 Composer 的 blockCommentsCollector 替换为同时接受三种注释类型的版本，
     * 确保所有注释事件在强转前被消费。TabooLib 的 YamlCommentLoader 已将行内注释转换为
     * 普通注释处理，因此不会丢失功能。
     */
    private static final Field composerBlockCommentsCollector;

    /**
     * 获取指定类中指定名称的字段。
     *
     * @param clazz 目标类
     * @param name 字段名称
     * @return 返回对应的 Field 对象，如果获取失败则返回 null
     */
    private static Field getDeclaredField(Class<?> clazz, String name) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            // 忽略异常，作为一个安全的回退机制
        }
        return field;
    }

    // 静态初始化块，用于初始化静态字段
    static {
        emitterEvents = getDeclaredField(Emitter.class, "events");
        emitterBlockCommentsCollector = getDeclaredField(Emitter.class, "blockCommentsCollector");
        emitterInlineCommentsCollector = getDeclaredField(Emitter.class, "inlineCommentsCollector");
        composerBlockCommentsCollector = getDeclaredField(Composer.class, "blockCommentsCollector");
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
     * 重写组合方法，修复 SnakeYAML 的注释事件处理缺陷。
     *
     * <p>SnakeYAML 的 {@code Composer.composeNode()} 中，{@code blockCommentsCollector}
     * 仅收集 {@code BLANK_LINE} 和 {@code BLOCK} 类型的注释事件。当 {@code ScannerImpl}
     * 将独占一行的注释误判为 {@code IN_LINE} 时，该事件不会被收集，导致后续的
     * {@code (NodeEvent) parser.peekEvent()} 强转抛出 {@code ClassCastException}。</p>
     *
     * <p>此方法通过反射将 {@code Composer} 的 {@code blockCommentsCollector} 替换为
     * 同时接受全部三种注释类型的版本，确保所有注释事件在强转前被消费。</p>
     *
     * @param input YAML 输入
     * @return 解析后的节点树
     */
    @Override
    public Node compose(@NotNull Reader input) {
        Parser parser = new ParserImpl(new StreamReader(input), loadingConfig);
        Composer composer = new Composer(parser, resolver, loadingConfig);
        if (composerBlockCommentsCollector != null) {
            try {
                // 替换为同时接受 BLANK_LINE、BLOCK、IN_LINE 三种注释类型的收集器
                composerBlockCommentsCollector.set(composer,
                        new CommentEventsCollector(parser, CommentType.BLANK_LINE, CommentType.BLOCK, CommentType.IN_LINE));
            } catch (ReflectiveOperationException ex) {
                // 反射失败时回退到默认行为
            }
        }
        return composer.getSingleNode();
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
        if (emitterEvents != null && emitterBlockCommentsCollector != null && emitterInlineCommentsCollector != null) {
            Queue<Event> newEvents = new ArrayDeque<>(100);
            try {
                emitterEvents.set(emitter, newEvents);
                emitterBlockCommentsCollector.set(emitter, new CommentEventsCollector(newEvents, CommentType.BLANK_LINE, CommentType.BLOCK));
                emitterInlineCommentsCollector.set(emitter, new CommentEventsCollector(newEvents, CommentType.IN_LINE));
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