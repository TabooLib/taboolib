package taboolib.common.exception;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    @Override
    public T getOrElse(T defaultValue) {
        return defaultValue;
    }

    @Override
    public T getOrElseGet(Supplier<T> defaultValue) {
        return defaultValue.get();
    }

    @Override
    public <U extends Throwable> T getOrElseThrow(Supplier<U> exceptionSupplier) throws U {
        throw exceptionSupplier.get();
    }

    @Override
    public <U extends Throwable> T getOrElseThrow(Class<U> exceptionClass) throws U {
        try {
            throw exceptionClass.getConstructor(Throwable.class).newInstance(value);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | SecurityException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ignored) {
            try {
                throw exceptionClass.getConstructor().newInstance().initCause(value);
            } catch (Throwable ex) {
                throw new IllegalStateException("Cannot create exception of type " + exceptionClass.getName(), ex);
            }
        }

        RuntimeException ex = new IllegalStateException("Cannot create exception of type " + exceptionClass.getName());
        ex.addSuppressed(value);

        throw ex;
    }

    @Override
    public <U> U withResultOrNull(Function<? super T, ? extends U> resultSupplier) {
        return null;
    }
}