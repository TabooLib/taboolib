package taboolib.module.configuration;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.*;
import taboolib.library.configuration.ConfigurationSection;
import taboolib.library.configuration.YamlConstructor;
import taboolib.library.configuration.YamlRepresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TabooLib
 * taboolib.module.configuration.YamlCommentLoader
 *
 * @author 坏黑
 * @since 2022/10/3 01:13
 */
@SuppressWarnings("FieldCanBeLocal")
public class YamlCommentLoader {

    private final DumperOptions yamlDumperOptions;
    private final LoaderOptions yamlLoaderOptions;
    private final YamlConstructor constructor;
    private final YamlRepresenter representer;
    private final Yaml yaml;

    public YamlCommentLoader(DumperOptions yamlDumperOptions, LoaderOptions yamlLoaderOptions, YamlConstructor constructor, YamlRepresenter representer, Yaml yaml) {
        this.yamlDumperOptions = yamlDumperOptions;
        this.yamlLoaderOptions = yamlLoaderOptions;
        this.constructor = constructor;
        this.representer = representer;
        this.yaml = yaml;
    }

    /**
     * This method splits the header on the last empty line, and sets the
     * comments below this line as comments for the first key on the map object.
     *
     * @param node The root node of the yaml object
     */
    public void adjustNodeComments(final MappingNode node) {
        if (node.getBlockComments() == null && !node.getValue().isEmpty()) {
            Node firstNode = node.getValue().get(0).getKeyNode();
            List<CommentLine> lines = firstNode.getBlockComments();
            if (lines != null) {
                int index = -1;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).getCommentType() == CommentType.BLANK_LINE) {
                        index = i;
                    }
                }
                if (index != -1) {
                    node.setBlockComments(lines.subList(0, index + 1));
                    firstNode.setBlockComments(lines.subList(index + 1, lines.size()));
                }
            }
        }
    }

    public void fromNodeTree(@NotNull MappingNode input, @NotNull ConfigurationSection section) {
        constructor.flattenMapping(input);
        for (NodeTuple nodeTuple : input.getValue()) {
            Node key = nodeTuple.getKeyNode();
            String keyString = String.valueOf(constructor.construct(key));
            Node value = nodeTuple.getValueNode();

            while (value instanceof AnchorNode) {
                value = ((AnchorNode) value).getRealNode();
            }

            if (value instanceof MappingNode) {
                fromNodeTree((MappingNode) value, section.createSection(keyString));
            } else {
                section.set(keyString, constructor.construct(value));
            }

            List<String> commentLines = getCommentLines(key.getBlockComments());
            if (!commentLines.isEmpty()) {
                section.setComments(keyString, commentLines);
            }

            // Unsupported inline comments
            // 内联注释会被转换为普通注释

            if (value instanceof MappingNode || value instanceof SequenceNode) {
                section.addComments(keyString, getCommentLines(key.getInLineComments()));
            } else {
                section.addComments(keyString, getCommentLines(value.getInLineComments()));
            }
        }
    }

    @SuppressWarnings("CommentedOutCode")
    public MappingNode toNodeTree(@NotNull ConfigurationSection section) {
        List<NodeTuple> nodeTuples = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            Node key = representer.represent(entry.getKey());
            Node value;
            if (entry.getValue() instanceof ConfigurationSection) {
                value = toNodeTree((ConfigurationSection) entry.getValue());
            } else {
                value = representer.represent(entry.getValue());
            }
            List<CommentLine> commentLines = getCommentLines(section.getComments(entry.getKey()), CommentType.BLOCK);
            if (!commentLines.isEmpty()) {
                key.setBlockComments(commentLines);
            }

            // Unsupported inline comments

//            if (value instanceof MappingNode || value instanceof SequenceNode) {
//                key.setInLineComments(getCommentLines(section.getInlineComments(entry.getKey()), CommentType.IN_LINE));
//            } else {
//                value.setInLineComments(getCommentLines(section.getInlineComments(entry.getKey()), CommentType.IN_LINE));
//            }

            nodeTuples.add(new NodeTuple(key, value));
        }

        return new MappingNode(Tag.MAP, nodeTuples, DumperOptions.FlowStyle.BLOCK);
    }

    public List<String> getCommentLines(List<CommentLine> comments) {
        List<String> lines = new ArrayList<>();
        if (comments != null) {
            for (CommentLine comment : comments) {
                if (comment.getCommentType() == CommentType.BLANK_LINE) {
                    lines.add(null);
                } else {
                    String line = comment.getValue();
                    line = line.startsWith(" ") ? line.substring(1) : line;
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    public List<CommentLine> getCommentLines(List<String> comments, CommentType commentType) {
        List<CommentLine> lines = new ArrayList<>();
        for (String comment : comments) {
            if (comment == null) {
                lines.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));
            } else {
                String line = comment;
                line = line.isEmpty() ? line : " " + line;
                lines.add(new CommentLine(null, null, line, commentType));
            }
        }
        return lines;
    }
}
