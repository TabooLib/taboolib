package taboolib.library.kether;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SimpleQuestLoader implements QuestLoader {

    @Override
    public <C extends QuestContext> Quest load(QuestService<C> service, String id, Path path, List<String> namespace) throws IOException {
        return load(service, id, Files.readAllBytes(path), namespace);
    }

    @Override
    public <CTX extends QuestContext> Quest load(QuestService<CTX> service, String id, byte[] bytes, List<String> namespace) throws LocalizedException {
        return newBlockReader(new String(bytes, StandardCharsets.UTF_8).toCharArray(), service, namespace).parse(id);
    }

    protected BlockReader newBlockReader(char[] content, QuestService<?> service, List<String> namespace) {
        return new BlockReader(content, service, namespace);
    }
}
