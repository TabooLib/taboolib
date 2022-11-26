package taboolib.module.kether

import taboolib.common5.Coerce
import java.util.concurrent.CompletableFuture

fun <T> CompletableFuture<Any?>.str(then: (String) -> T): CompletableFuture<T> {
    return thenApply { then(it?.toString()?.trimIndent() ?: "") }
}

fun <T> CompletableFuture<Any?>.strOrNull(then: (String?) -> T): CompletableFuture<T> {
    return thenApply { then(it?.toString()?.trimIndent()) }
}

fun <T> CompletableFuture<Any?>.bool(then: (Boolean) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toBoolean(it)) }
}

fun <T> CompletableFuture<Any?>.int(then: (Int) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toInteger(it)) }
}

fun <T> CompletableFuture<Any?>.long(then: (Long) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toLong(it)) }
}

fun <T> CompletableFuture<Any?>.double(then: (Double) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toDouble(it)) }
}

fun <T> CompletableFuture<Any?>.float(then: (Float) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toFloat(it)) }
}