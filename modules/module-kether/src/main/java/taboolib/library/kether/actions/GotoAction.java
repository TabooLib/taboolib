package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.QuestAction;
import taboolib.library.kether.QuestActionParser;
import taboolib.library.kether.QuestContext;

import java.util.concurrent.CompletableFuture;

final class GotoAction extends QuestAction<Void> {

    private final String block;

    public GotoAction(String block) {
        this.block = block;
    }

    @Override
    public CompletableFuture<Void> process(@NotNull QuestContext.Frame frame) {
        frame.setNext(frame.context().getQuest().getBlocks().get(block));
        return CompletableFuture.completedFuture(null);
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(resolver -> new GotoAction(resolver.nextToken()));
    }
}
