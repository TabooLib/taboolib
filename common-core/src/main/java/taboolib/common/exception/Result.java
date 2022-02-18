package taboolib.common.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

public class Result<T> {

    private static final Result<?> empty = new Result<Void>(null, null, true);

    private final T data;

    private final boolean isSuccess;

    private final Throwable throwable;

    public Result(T data, Throwable throwable, boolean isSuccess) {
        this.data = data;
        this.isSuccess = isSuccess;
        this.throwable = throwable;
    }

    public static Result<?> empty() {
        return empty;
    }

    @NotNull
    public static <U> Result<? extends U> success(U data) {
        return new Result<>(data, null, true);
    }

    @NotNull
    public static <U> Result<? extends U> failure(Throwable ex) {
        return new Result<>(null, ex, false);
    }

    @SafeVarargs
    public final boolean isExceptionOf(Class<? extends Throwable>... classes) {
        if (throwable == null) return false;
        return Arrays.stream(classes).anyMatch(clazz -> clazz.isAssignableFrom(throwable.getClass()));
    }

    @SafeVarargs
    public final <U extends Throwable> boolean isExceptionOf(U... types) {
        // get type from array
        Class<?> clazz = types.getClass().getComponentType();
        if (!clazz.isAssignableFrom(Throwable.class)) return false;

        // noinspection unchecked
        return isExceptionOf((Class<Throwable>) clazz);
    }

    @SafeVarargs
    public final Result<?> takeIfExceptionOf(Class<? extends Throwable>... classes) {
        return isExceptionOf(classes) ? this : empty;
    }

    @SafeVarargs
    public final <U extends Throwable> Result<?> takeIfType(U... types) {
        return isExceptionOf(types) ? this : empty;
    }

    @SafeVarargs
    public final Result<?> takeUnlessExceptionOf(Class<? extends Throwable>... classes) {
        return isExceptionOf(classes) ? empty : this;
    }

    @SafeVarargs
    public final <U extends Throwable> Result<?> takeUnlessType(U... types) {
        return isExceptionOf(types) ? empty : this;
    }

    public void onSuccess(Consumer<T> block) {
        if (isSuccess && data != null) block.accept(data);
    }

    public void onFailure(Consumer<? super Throwable> block) {
        if (!isSuccess && throwable != null) block.accept(throwable);
    }

    public Result<T> printIfFailure() {
        if (!isSuccess && throwable != null) throwable.printStackTrace();
        return this;
    }

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