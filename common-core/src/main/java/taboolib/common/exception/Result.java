package taboolib.common.exception;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Result<T> {
    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    boolean isExceptionOf(Collection<Class<? extends Throwable>> classes);

    Result<T> takeIfExceptionOf(Collection<Class<? extends Throwable>> classes);

    default Result<T> takeIfExceptionOf(Class<? extends Throwable> clazz) {
        return takeIfExceptionOf(Collections.singleton(clazz));
    }

    Result<T> takeUnlessExceptionOf(Collection<Class<? extends Throwable>> classes);

    default Result<T> takeUnlessExceptionOf(Class<? extends Throwable> clazz) {
        return takeUnlessExceptionOf(Collections.singleton(clazz));
    }

    Result<T> onSuccess(Consumer<T> block);

    Result<T> onFailure(Consumer<? super Throwable> block);

    Result<T> printIfFailure();

    T unwrap();

    T getOrElse(T defaultValue);

    T getOrElseGet(Supplier<T> defaultValue);

    <U extends Throwable> T getOrElseThrow(Supplier<U> exceptionSupplier) throws U;

    <U extends Throwable> T getOrElseThrow(Class<U> exceptionClass) throws U;

    <U> U withResultOrNull(Function<? super T, ? extends U> resultSupplier);
}
