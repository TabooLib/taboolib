package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Quest {

    String getId();

    Optional<Block> getBlock(@NotNull String label);

    Map<String, Block> getBlocks();

    Optional<Block> blockOf(@NotNull ParsedAction<?> action);

    interface Block {

        String getLabel();

        List<ParsedAction<?>> getActions();

        int indexOf(@NotNull ParsedAction<?> action);

        Optional<ParsedAction<?>> get(int i);
    }
}
