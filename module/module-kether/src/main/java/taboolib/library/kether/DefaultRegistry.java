package taboolib.library.kether;

import java.util.*;
import java.util.function.BiFunction;

public class DefaultRegistry implements QuestRegistry {

    private final Map<String, Map<String, QuestActionParser>> parsers = new HashMap<>();
    private final Map<String, BiFunction<QuestContext.Frame, String, String>> processors = new HashMap<>();

    @Override
    public void registerAction(String namespace, String id, QuestActionParser parser) {
        parsers.computeIfAbsent(namespace, i -> new HashMap<>()).put(id, parser);
    }

    @Override
    public void registerAction(String id, QuestActionParser parser) {
        registerAction("kether", id, parser);
    }

    @Override
    public void registerStringProcessor(String id, BiFunction<QuestContext.Frame, String, String> processor) {
        processors.put(id, processor);
    }

    @Override
    public void unregisterAction(String id) {
        unregisterAction("kether", id);
    }

    @Override
    public void unregisterAction(String namespace, String id) {
        Map<String, QuestActionParser> map = parsers.computeIfAbsent(namespace, i -> new HashMap<>());
        if (id.equals("*")) {
            map.clear();
        } else {
            map.remove(id);
        }
    }

    @Override
    public Collection<String> getRegisteredActions(String namespace) {
        Map<String, QuestActionParser> map = parsers.get(namespace);
        return map == null ? Collections.emptyList() : Collections.unmodifiableCollection(map.keySet());
    }

    @Override
    public Collection<String> getRegisteredActions() {
        return getRegisteredActions("kether");
    }

    @Override
    public Collection<String> getRegisteredNamespace() {
        return Collections.unmodifiableCollection(parsers.keySet());
    }

    @Override
    public Optional<QuestActionParser> getParser(String id, String namespace) {
        Map<String, QuestActionParser> map = parsers.get(namespace);
        return map == null ? Optional.empty() : Optional.ofNullable(map.get(id));
    }

    @Override
    public Optional<QuestActionParser> getParser(String id, List<String> namespace) {
        String[] domain = id.split(":");
        if (domain.length == 2) {
            return getParser(domain[1], domain[0]);
        } else {
            for (String name : namespace) {
                Optional<QuestActionParser> optional = getParser(id, name);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<QuestActionParser> getParser(String id) {
        return getParser(id, "kether");
    }

    @Override
    public Optional<BiFunction<QuestContext.Frame, String, String>> getStringProcessor(String id) {
        return Optional.ofNullable(processors.get(id));
    }
}
