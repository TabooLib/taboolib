package taboolib.library.kether;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public interface QuestRegistry {

    void registerAction(String id, QuestActionParser parser);

    void registerAction(String namespace, String id, QuestActionParser parser);

    void registerStringProcessor(String id, BiFunction<QuestContext.Frame, String, String> processor);

    void unregisterAction(String id);

    void unregisterAction(String namespace, String id);

    Collection<String> getRegisteredActions(String namespace);

    Collection<String> getRegisteredActions();

    Collection<String> getRegisteredNamespace();

    Optional<QuestActionParser> getParser(String id, List<String> namespace);

    Optional<QuestActionParser> getParser(String id, String namespace);

    Optional<QuestActionParser> getParser(String id);

    Optional<BiFunction<QuestContext.Frame, String, String>> getStringProcessor(String id);
}
