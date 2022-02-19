package taboolib.common.exception;

import java.util.Collection;
import java.util.function.Consumer;

public class FailureResult<T> implements Result<T> {

    @SuppressWarnings({"rawtypes"})
    private static final FailureResult<?> empty = new FailureResult(null);

    Throwable value;

    FailureResult(Throwable value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    private static <U> FailureResult<U> empty() {
        return (FailureResult<U>) empty;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isExceptionOf(Collection<Class<? extends Throwable>> classes) {
        return classes.parallelStream().anyMatch(it -> value.getClass().isAssignableFrom(it));
    }

    @Override
    public Result<T> takeIfExceptionOf(Collection<Class<? extends Throwable>> classes) {
        return isExceptionOf(classes) ? this : empty();
    }

    @Override
    public Result<T> takeUnlessExceptionOf(Collection<Class<? extends Throwable>> classes) {
        return isExceptionOf(classes) ? null : empty();
    }

    @Override
    public Result<T> onSuccess(Consumer<T> block) {
        return this;
    }

    @Override
    public Result<T> onFailure(Consumer<? super Throwable> block) {
        block.accept(value);
        return this;
    }

    @Override
    public Result<T> printIfFailure() {
        value.printStackTrace();
        return this;
    }

    @Override
    public T unwrap() {
        return null;
    }
}