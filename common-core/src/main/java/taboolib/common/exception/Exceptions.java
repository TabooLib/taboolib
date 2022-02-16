package taboolib.common.exception;

import java.util.function.Supplier;

public class Exceptions {

    public static <T> Result<? extends T> runCatching(Supplier<? extends T> block) {
        try {
            return Result.success(block.get());
        } catch (Throwable e) {
            return Result.failure(e);
        }
    }

    public static Result<?> runCatching(Runnable block) {
        try {
            block.run();
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}
