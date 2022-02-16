@file:Suppress("TooManyFunctions")
package taboolib.common.stream

import java.util.*
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

fun <T> Array<T>.stream(): Stream<T> = Arrays.stream(this)

fun <T> Array<T>.parallelStream(): Stream<T> = Arrays.stream(this).parallel()

fun IntArray.stream(): IntStream = Arrays.stream(this)

fun IntArray.parallelStream(): IntStream = Arrays.stream(this).parallel()

fun DoubleArray.stream(): DoubleStream = Arrays.stream(this)

fun DoubleArray.parallelStream(): DoubleStream = Arrays.stream(this).parallel()

fun LongArray.stream(): LongStream = Arrays.stream(this)

fun LongArray.parallelStream(): LongStream = Arrays.stream(this).parallel()

fun <T> Stream<T>.mapFlattened(mapper: (T) -> Collection<T>): Stream<T> =
    flatMap { mapper(it).stream() }

fun <T> Stream<T>.mapFlattenedParalleled(mapper: (T) -> Collection<T>): Stream<T> =
    flatMap { mapper(it).parallelStream() }

inline fun <reified T> Stream<T>.toTypedArray() = toArray<T> { arrayOfNulls(0) }

fun <T> Sequence<T>.peek(action: (T) -> Unit) = map {
    Objects.hash()
    action(it)
    it
}
