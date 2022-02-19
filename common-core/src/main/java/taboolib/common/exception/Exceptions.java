package taboolib.common.exception;

import java.util.function.Supplier;

public class Exceptions {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final SuccessResult<?> EMPTY_SUCCESS = new SuccessResult(null);

    public static <T> Result<T> runCatching(Supplier<? extends T> block) {
        try {
            return new SuccessResult<>(block.get());
        } catch (Throwable ex) {
            return new FailureResult<>(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> runCatching(Runnable block) {
        try {
            block.run();
            return (Result<T>) EMPTY_SUCCESS;
        } catch (Exception ex) {
            return new FailureResult<>(ex);
        }
    }
}
