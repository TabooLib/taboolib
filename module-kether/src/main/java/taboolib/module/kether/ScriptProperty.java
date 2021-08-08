package taboolib.module.kether;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * TabooLib
 * taboolib.module.kether.ScriptProperty
 *
 * @author sky
 * @since 2021/8/9 12:24 上午
 */
public abstract class ScriptProperty implements Serializable {

    private static final long serialVersionUID = -5093163316182979437L;

    private final String id;

    public ScriptProperty(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    abstract public OperationResult read(@NotNull Object instance, @NotNull String key);

    abstract public OperationResult write(@NotNull Object instance, @NotNull String key, @Nullable Object value);

    public static class OperationResult {

        private final boolean successful;
        private final Object value;

        public OperationResult(boolean successful, Object value) {
            this.successful = successful;
            this.value = value;
        }

        public boolean isSuccessful() {
            return successful;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        @NotNull
        public static OperationResult successful() {
            return new OperationResult(true, null);
        }

        @NotNull
        public static OperationResult successful(@Nullable Object value) {
            return new OperationResult(true, value);
        }

        @NotNull
        public static OperationResult failed() {
            return new OperationResult(false, null);
        }
    }
}