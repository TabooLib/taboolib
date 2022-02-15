@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

fun random(): Random =
    ThreadLocalRandom.current()

fun random(v: Double) =
    ThreadLocalRandom.current().nextDouble() <= v

fun random(v: Int) =
    ThreadLocalRandom.current().nextInt(v)

fun random(num1: Int, num2: Int): Int {
    val min = min(num1, num2)
    val max = max(num1, num2)
    return ThreadLocalRandom.current().nextInt(min, max + 1)
}

fun random(num1: Double, num2: Double): Double {
    val min = min(num1, num2)
    val max = max(num1, num2)
    return if (min == max) max else ThreadLocalRandom.current().nextDouble(min, max)
}