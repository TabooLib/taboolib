package taboolib.library.kether.actions;

import taboolib.library.kether.*;

public class KetherTypes {

    public static <C extends QuestContext> void registerInternals(QuestRegistry registry, QuestService<C> service) {
        registry.registerAction("noop", QuestActionParser.of(r -> QuestAction.noop()));
        registry.registerAction("async", AsyncAction.parser(service));
        registry.registerAction("await", AwaitAction.parser(service));
        registry.registerAction("await_all", AwaitAllAction.parser(service));
        registry.registerAction("await_any", AwaitAnyAction.parser(service));
        registry.registerAction("call", CallAction.parser());
        registry.registerAction("if", IfAction.parser(service));
        registry.registerAction("goto", GotoAction.parser());
        registry.registerAction("while", WhileAction.parser(service));
        registry.registerAction("repeat", RepeatAction.parser(service));
        registry.registerAction("exit", ExitAction.parser());
        registry.registerAction("all", AllAction.parser(service));
        registry.registerAction("any", AnyAction.parser(service));
        registry.registerAction("not", NotAction.parser(service));
        registry.registerAction("literal", LiteralAction.parser());
    }
}
