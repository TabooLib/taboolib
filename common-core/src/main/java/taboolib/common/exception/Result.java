package taboolib.common.exception;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

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
}
