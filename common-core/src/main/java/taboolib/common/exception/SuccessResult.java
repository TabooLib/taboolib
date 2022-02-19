package taboolib.common.exception;

import java.util.Collection;
import java.util.function.Consumer;

public class SuccessResult<T> implements Result<T> {

    T value;

    private SuccessResult() {}

    SuccessResult(T value) {
        this.value = value;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isExceptionOf(Collection<Class<? extends Throwable>> classes) {
        return false;
    }

    @Override
    public Result<T> takeIfExceptionOf(Collection<Class<? extends Throwable>> classes) {
        return this;
    }

    @Override
    public Result<T> takeUnlessExceptionOf(Collection<Class<? extends Throwable>> classes) {
        return this;
    }

    @Override
    public Result<T> onSuccess(Consumer<T> block) {
        block.accept(value);
        return this;
    }

    @Override
    public Result<T> onFailure(Consumer<? super Throwable> block) {
        return this;
    }

    @Override
    public Result<T> printIfFailure() {
        return this;
    }

    @Override
    public T unwrap() {
        return value;
    }
}
