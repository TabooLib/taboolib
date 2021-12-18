package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.actions.LiteralAction;
import taboolib.module.kether.Kether;
import taboolib.module.kether.action.ActionGet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleReader extends AbstractStringReader implements QuestReader {

    protected final List<String> namespace;
    protected final QuestService<?> service;
    protected final BlockReader blockParser;

    public SimpleReader(QuestService<?> service, BlockReader reader, List<String> namespace) {
        super(reader.content);
        this.service = service;
        this.blockParser = reader;
        this.index = reader.index;
        this.namespace = new ArrayList<>(namespace);
        this.namespace.add("kether");
    }

    @Override
    public String nextToken() {
        skipBlank();
        switch (peek()) {
            case '"': {
                int cnt = 0;
                while (peek() == '"') {
                    cnt++;
                    skip(1);
                }
                int met = 0;
                int i;
                for (i = index; i < content.length; ++i) {
                    if (content[i] == '"') met++;
                    else {
                        if (met >= cnt) break;
                        else met = 0;
                    }
                }
                if (met < cnt) throw LoadError.STRING_NOT_CLOSE.create(cnt);
                String ret = new String(content, index, i - cnt - index);
                index = i;
                return ret;
            }
            case '\'': {
                skip(1);
                int i = index;
                while (peek() != '\'') {
                    skip(1);
                }
                String ret = new String(content, i, index - i);
                skip(1);
                return ret;
            }
            default: {
                return super.nextToken();
            }
        }
    }

    protected ParsedAction<?> nextAnonAction() {
        ParsedAction<?> parsedAction = blockParser.readAnonymousAction();
        parsedAction.set(ActionProperties.REQUIRE_FRAME, true);
        return parsedAction;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParsedAction<T> nextAction() {
        skipBlank();
        switch (peek()) {
            case '{': {
                blockParser.index = this.index;
                ParsedAction<?> action = nextAnonAction();
                this.index = blockParser.index;
                return (ParsedAction<T>) action;
            }
            case '&': {
                skip(1);
                beforeParse();
                return wrap(new ActionGet<>(nextToken()));
            }
            case '*': {
                skip(1);
                beforeParse();
                return wrap(new LiteralAction<>(nextToken()));
            }
            default: {
                String element = nextToken();
                Optional<QuestActionParser> optional = service.getRegistry().getParser(element, namespace);
                if (optional.isPresent()) {
                    beforeParse();
                    return wrap(optional.get().resolve(this));
                } else if (Kether.INSTANCE.isAllowToleranceParser()) {
                    beforeParse();
                    return wrap(new LiteralAction<>(element));
                }
                throw LoadError.UNKNOWN_ACTION.create(element);
            }
        }
    }

    protected void beforeParse() {
    }

    protected <T> ParsedAction<T> wrap(QuestAction<T> action) {
        return new ParsedAction<>(action);
    }

    @Override
    public void expect(@NotNull String value) {
        super.expect(value);
    }

    public List<String> getNamespace() {
        return namespace;
    }

    public QuestService<?> getService() {
        return service;
    }

    public BlockReader getBlockParser() {
        return blockParser;
    }
}
