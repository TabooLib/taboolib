package taboolib.module.kether

import taboolib.common5.Coerce
import java.util.concurrent.CompletableFuture

fun <T> CompletableFuture<Any?>.str(then: (String) -> T): CompletableFuture<T> {
    return thenApply { then(it?.toString()?.trimIndent() ?: "") }.except { then("") }
}

fun <T> CompletableFuture<Any?>.strOrNull(then: (String?) -> T): CompletableFuture<T> {
    return thenApply { then(it?.toString()?.trimIndent()) }.except { then(null) }
}

fun <T> CompletableFuture<Any?>.bool(then: (Boolean) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toBoolean(it)) }.except { then(false) }
}

fun <T> CompletableFuture<Any?>.int(then: (Int) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toInteger(it)) }.except { then(0) }
}

fun <T> CompletableFuture<Any?>.long(then: (Long) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toLong(it)) }.except { then(0) }
}

fun <T> CompletableFuture<Any?>.double(then: (Double) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toDouble(it)) }.except { then(0.0) }
}

fun <T> CompletableFuture<Any?>.float(then: (Float) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toFloat(it)) }.except { then(0.0f) }
}

fun <T> CompletableFuture<T>.orNull(): T? {
    return getNow(null)
}

fun <T> CompletableFuture<T>.except(): CompletableFuture<T> {
    return exceptionally {
        it.printStackTrace()
        null
    }
}

fun <T> CompletableFuture<T>.except(fn: (Throwable) -> T): CompletableFuture<T> {
    return exceptionally {
        it.printStackTrace()
        fn(it)
    }
}

fun <T> CompletableFuture<T>.exceptNull(fn: (Throwable) -> Unit): CompletableFuture<T> {
    return exceptionally {
        it.printStackTrace()
        fn(it)
        null
    }
}