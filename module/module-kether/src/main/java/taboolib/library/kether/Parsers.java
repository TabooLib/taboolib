package taboolib.library.kether;

import kotlin.Pair;
import taboolib.common5.Coerce;

import javax.annotation.Nullable;
import java.util.Optional;

// 要是 Java 能直接调 Kt 那边的，可以扔到 ParserDSL 那个类去
public enum Parsers {

    ;

    public static Parser<Integer> integer() {
        return Parser.frame(r -> {
            r.mark();
            try {
                int i = r.nextInt();
                return Parser.Action.point(i);
            } catch (Exception e) {
                r.reset();
                ParsedAction<?> action = r.nextParsedAction();
                return frame -> action.process(frame).thenApply(Coerce::toInteger);
            }
        });
    }

    public static Parser<Double> decimal() {
        return Parser.frame(r -> {
            r.mark();
            try {
                double i = r.nextDouble();
                return Parser.Action.point(i);
            } catch (Exception e) {
                r.reset();
                ParsedAction<?> action = r.nextParsedAction();
                return frame -> action.process(frame).thenApply(Coerce::toDouble);
            }
        });
    }

    public static Parser<String> symbol() {
        return Parser.of(QuestReader::nextToken);
    }

    public static Parser<String> string() {
        return Parser.frame(r -> {
            r.mark();
            try {
                ParsedAction<?> action = r.nextParsedAction();
                return frame -> action.process(frame).thenApply(Coerce::toString);
            } catch (Exception e) {
                r.reset();
                return Parser.Action.point(r.nextToken());
            }
        });
    }

    public static <A> Parser<A> command(String s, Parser<A> then) {
        return Parser.frame(r -> {
            r.expect(s);
            return then.reader.apply(r);
        });
    }

    public static <A, B> Parser<Pair<A, B>> and(Parser<A> a, Parser<B> b) {
        return Parser.frame(r -> {
            Parser.Action<A> actionA = a.reader.apply(r);
            Parser.Action<B> actionB = b.reader.apply(r);
            return frame -> actionA.run(frame).thenCompose(a1 -> actionB.run(frame).thenApply(b1 -> new Pair<>(a1, b1)));
        });
    }
}
