package taboolib.library.kether;

public class QuestCloseException extends RuntimeException {

    public QuestCloseException() {
    }

    public QuestCloseException(String message) {
        super(message);
    }

    public QuestCloseException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuestCloseException(Throwable cause) {
        super(cause);
    }

    public QuestCloseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
