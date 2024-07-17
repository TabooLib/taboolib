package taboolib.library.kether;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public interface QuestLoader {

    default <C extends QuestContext> Quest load(QuestService<C> service, String id, byte[] bytes) throws LocalizedException {
        return load(service, id, bytes, new ArrayList<>());
    }

    <C extends QuestContext> Quest load(QuestService<C> service, String id, Path path, List<String> namespace) throws IOException, LocalizedException;

    <C extends QuestContext> Quest load(QuestService<C> service, String id, byte[] bytes, List<String> namespace) throws LocalizedException;
}
