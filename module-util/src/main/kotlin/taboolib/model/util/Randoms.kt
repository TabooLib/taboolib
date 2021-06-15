package taboolib.model.util

import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

/**
 * @author sky
 * @since 2020-10-1 10:43
 */
class Randoms<T>(vararg element: Pair<T, Int>) {

    private val value = CopyOnWriteArrayList<Value<T>>()

    init {
        element.forEach {
            add(it.first, it.second)
        }
    }

    fun random(): T? {
        val sum = value.sumBy { it.index }
        if (sum > 0) {
            var m = 0
            val n = Random().nextInt(sum)
            for (obj in value) {
                if (m <= n && n < m + obj.index) {
                    return obj.element
                }
                m += obj.index
            }
        }
        return null
    }

    fun add(element: T, index: Int = 1) {
        value.add(Value(element, index))
    }

    fun remove(element: T) {
        value.removeIf { it.element == element }
    }

    fun values(): MutableList<Value<T>> {
        return value
    }

    fun size(): Int {
        return value.size
    }

    data class Value<T>(val element: T, val index: Int)
}
