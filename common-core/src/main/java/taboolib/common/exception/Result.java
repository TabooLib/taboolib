package taboolib.common.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Result<T> {

    private final T data;

    private final boolean isSuccess;

    private final Throwable throwable;

    public Result(T data, Throwable throwable, boolean isSuccess) {
        this.data = data;
        this.isSuccess = isSuccess;
        this.throwable = throwable;
    }

    @NotNull
    public static <U> Result<? extends U> success(U data) {
        return new Result<>(data, null, true);
    }

    @NotNull
    public static <U> Result<? extends U> failure(Throwable ex) {
        return new Result<>(null, ex, false);
    }

    public <U extends Throwable> boolean isExceptionOf(U[] types) {
        // get type from array
        Class<?> clazz = types.getClass().getComponentType();
        return throwable != null && throwable.getClass().isAssignableFrom(clazz);
    }

    public void onSuccess(Consumer<T> block) {
        if (isSuccess && data != null) block.accept(data);
    }

    public void onFailure(Consumer<? super Throwable> block) {
        if (!isSuccess && throwable != null) block.accept(throwable);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Nullable
    public T unwrap() {
        if (isSuccess && data != null) return data;

        throwable.printStackTrace();
        return null;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}