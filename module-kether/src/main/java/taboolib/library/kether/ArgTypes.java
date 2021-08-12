package taboolib.library.kether;

import taboolib.common5.Coerce;

import java.time.Duration;
import java.util.List;

public class ArgTypes {

    public static final ArgType<Integer> INT = new CoerceType<>(Coerce::asInteger, "integer");

    public static final ArgType<Long> LONG = new CoerceType<>(Coerce::asLong, "long");

    public static final ArgType<Double> DOUBLE = new CoerceType<>(Coerce::asDouble, "double");

    public static final ArgType<Boolean> BOOLEAN = new CoerceType<>(Coerce::asBoolean, "boolean");

    public static final ArgType<Duration> DURATION = new DurationType();

    public static final ArgType<ParsedAction<?>> ACTION = QuestReader::nextAction;

    public static <T> ArgType<List<T>> listOf(ArgType<T> argType) {
        return new ListType<>(argType);
    }
}
