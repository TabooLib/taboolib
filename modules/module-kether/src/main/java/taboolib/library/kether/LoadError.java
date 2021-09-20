package taboolib.library.kether;

public enum LoadError {

    STRING_NOT_CLOSE,
    NOT_MATCH,
    UNKNOWN_ACTION,
    NOT_DURATION,
    EOF,
    BLOCK_ERROR,
    UNHANDLED;

    public LocalizedException create(Object... args) {
        return LocalizedException.of("load-error." + name().toLowerCase().replace("_", "-"), args);
    }
}
