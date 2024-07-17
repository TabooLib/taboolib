package taboolib.library.kether;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleQuest implements Quest {

    private final char[] content;
    private final String id;
    private final Map<String, Block> map = Maps.newHashMap();

    public SimpleQuest(char[] content, Map<String, Block> map, String id) {
        this.content = content;
        this.id = id;
        this.map.putAll(map);
    }

    public char[] getContent() {
        return content;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<Block> getBlock(@NotNull String label) {
        return Optional.ofNullable(map.get(label));
    }

    @Override
    public Map<String, Block> getBlocks() {
        return Collections.unmodifiableMap(this.map);
    }

    @Override
    public Optional<Block> blockOf(@NotNull ParsedAction<?> action) {
        if (action.has(ActionProperties.BLOCK)) {
            String s = action.get(ActionProperties.BLOCK);
            Block block = map.get(s);
            if (block.getActions().contains(action)) {
                return Optional.of(block);
            } else {
                return Optional.empty();
            }
        } else {
            for (Block block : map.values()) {
                if (block.getActions().contains(action)) {
                    return Optional.of(block);
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "SimpleQuest{" +
            "id='" + id + '\'' +
            ", map=" + map +
            '}';
    }

    public static class SimpleBlock implements Block {

        private final String label;
        private final List<ParsedAction<?>> actions;

        public SimpleBlock(String label, List<ParsedAction<?>> actions) {
            this.label = label;
            this.actions = actions;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public List<ParsedAction<?>> getActions() {
            return this.actions;
        }

        @Override
        public int indexOf(@NotNull ParsedAction<?> action) {
            return actions.indexOf(action);
        }

        @Override
        public Optional<ParsedAction<?>> get(int i) {
            if (i >= 0 && i < actions.size()) {
                return Optional.of(actions.get(i));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public String toString() {
            return "SimpleBlock{" +
                "label='" + label + '\'' +
                ", actions=" + actions +
                '}';
        }
    }
}
