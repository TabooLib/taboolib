package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.QuestAction;
import taboolib.library.kether.QuestActionParser;
import taboolib.library.kether.QuestContext;

import java.util.concurrent.CompletableFuture;

final class CallAction extends QuestAction<Object> {

    private final String block;

    public CallAction(String block) {
        this.block = block;
    }

    @Override
    public CompletableFuture<Object> process(@NotNull QuestContext.Frame frame) {
        QuestContext.Frame newFrame = frame.newFrame(block);
        newFrame.setNext(frame.context().getQuest().getBlocks().get(block));
        frame.addClosable(newFrame);
        return newFrame.run();
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(resolver -> new CallAction(resolver.nextToken()));
    }
}
