package taboolib.module.kether;

import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.loader.QuestReader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * TabooLib
 * taboolib.module.kether.ScriptActionParser
 *
 * @author sky
 * @since 2021/8/9 12:29 上午
 */
public class ScriptActionParser implements QuestActionParser, Serializable {

    private static final long serialVersionUID = -8286842279981581803L;

    private final Function<QuestReader, QuestAction<?>> resolve;

    public ScriptActionParser(Function<QuestReader, QuestAction<?>> resolve) {
        this.resolve = resolve;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> QuestAction<T> resolve(QuestReader questReader) {
        return (QuestAction<T>) resolve.apply(questReader);
    }

    @Override
    public List<String> complete(List<String> list) {
        return new ArrayList<>();
    }

    public Function<QuestReader, QuestAction<?>> getResolve() {
        return resolve;
    }
}
