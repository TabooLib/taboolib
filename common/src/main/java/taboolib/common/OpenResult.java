package taboolib.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenResult {

    private final boolean successful;
    private final Object value;

    public OpenResult(boolean successful, @Nullable Object value) {
        this.successful = successful;
        this.value = value;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isFailed() {
        return !successful;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    @NotNull
    public static OpenResult successful() {
        return new OpenResult(true, null);
    }

    @NotNull
    public static OpenResult successful(@Nullable Object value) {
        return new OpenResult(true, value);
    }

    @NotNull
    public static OpenResult failed() {
        return new OpenResult(false, null);
    }
}