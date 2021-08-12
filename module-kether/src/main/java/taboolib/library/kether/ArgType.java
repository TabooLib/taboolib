package taboolib.library.kether;

public interface ArgType<T> {

    T read(QuestReader reader) throws LocalizedException;
}
