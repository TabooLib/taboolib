package taboolib.library.kether;

import taboolib.library.kether.actions.LiteralAction;
import taboolib.module.kether.Kether;
import taboolib.module.kether.KetherError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockReader extends AbstractStringReader {

    protected final Map<String, Quest.Block> blocks;
    protected final QuestService<?> service;
    protected final List<String> namespace;
    protected String currentBlock;

    public BlockReader(char[] content, QuestService<?> service, List<String> namespace) {
        super(content);
        this.blocks = new HashMap<>();
        this.service = service;
        this.namespace = namespace;
    }

    public BlockReader(char[] arr, int index, int mark, Map<String, Quest.Block> blocks, QuestService<?> service, List<String> namespace, String currentBlock) {
        super(arr, index, mark);
        this.blocks = blocks;
        this.service = service;
        this.namespace = namespace;
        this.currentBlock = currentBlock;
    }

    public Quest parse(String id) {
        while (hasNext()) {
            readBlock();
        }
        return new SimpleQuest(content, blocks, id);
    }

    public void readBlock() {
        expect("def");
        String name = nextToken();
        expect("=");
        this.currentBlock = name;
        List<ParsedAction<?>> actions = readActions();
        checkLiteral(actions);
        SimpleQuest.SimpleBlock block = new SimpleQuest.SimpleBlock(name, actions);
        this.processActions(block, actions);
        this.blocks.put(name, block);
    }

    public void checkLiteral(List<ParsedAction<?>> actions) {
        if (Kether.INSTANCE.isAllowToleranceParser()) {
            LiteralAction<?> before = null;
            for (ParsedAction<?> action : actions) {
                if (before != null) {
                    throw KetherError.CUSTOM.create("Isolate literal \"" + before.getValue() + "\" is not end of block, maybe a misspelled action?");
                }
                if (action.getAction() instanceof LiteralAction) {
                    before = (LiteralAction<?>) action.getAction();
                }
            }
        }
    }

    public List<ParsedAction<?>> readActions() {
        skipBlank();
        boolean batch = peek() == '{';
        if (batch) skip(1);
        SimpleReader reader = newActionReader(service, namespace);
        try {
            ArrayList<ParsedAction<?>> list = new ArrayList<>();
            while ((batch && reader.hasNext()) || list.isEmpty()) {
                if (batch && reader.peek() == '}') {
                    reader.skip(1);
                    this.index = reader.index;
                    list.trimToSize();
                    return list;
                }
                list.add(reader.nextAction());
                reader.mark();
            }
            return list;
        } catch (Exception e) {
            if (e instanceof LocalizedException) {
                String source = new String(this.content, reader.getMark(), Math.min(this.content.length, reader.getIndex()) - reader.getMark()).trim();
                throw LoadError.BLOCK_ERROR.create(this.currentBlock, lineOf(this.content, reader.getMark()), source).then((LocalizedException) e);
            } else {
                e.printStackTrace();
                throw LoadError.UNHANDLED.create(e);
            }
        }
    }

    protected ParsedAction<?> readAnonymousAction() {
        String lastBlock = this.currentBlock;
        String name = nextAnonymousBlockName();
        this.currentBlock = name;
        List<ParsedAction<?>> actions = readActions();
        checkLiteral(actions);
        this.currentBlock = lastBlock;
        if (!actions.isEmpty()) {
            ParsedAction<?> head = actions.get(0);
            Quest.Block block = new SimpleQuest.SimpleBlock(name, actions);
            this.blocks.put(block.getLabel(), block);
            head.set(ActionProperties.BLOCK, block.getLabel());
            return head;
        } else {
            return ParsedAction.noop();
        }
    }

    protected SimpleReader newActionReader(QuestService<?> service, List<String> namespace) {
        return new SimpleReader(service, this, namespace);
    }

    protected String nextAnonymousBlockName() {
        return this.currentBlock + "_anon_" + System.nanoTime();
    }

    protected void processActions(Quest.Block block, List<ParsedAction<?>> actions) {
        if (!actions.isEmpty()) {
            actions.get(0).set(ActionProperties.BLOCK, block.getLabel());
        }
    }

    protected static int lineOf(char[] chars, int index) {
        int line = 1;
        for (int i = 0; i < index; i++) {
            if (chars[i] == '\n') line++;
        }
        return line;
    }

    public Map<String, Quest.Block> getBlocks() {
        return blocks;
    }

    public QuestService<?> getService() {
        return service;
    }

    public List<String> getNamespace() {
        return namespace;
    }

    public String getCurrentBlock() {
        return currentBlock;
    }
}