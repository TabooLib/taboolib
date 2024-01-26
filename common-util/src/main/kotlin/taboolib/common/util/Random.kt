package taboolib.common.util

import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

/**
 * 创建线程安全的随机数生成器
 */
fun random(): Random {
    return ThreadLocalRandom.current()
}

/**
 * 随机数判断
 *
 * @param v 0-1
 */
fun random(v: Double): Boolean {
    return ThreadLocalRandom.current().nextDouble() <= v
}

/**
 * 随机生成整数
 *
 * @param v 最大值
 */
fun random(v: Int): Int {
    return ThreadLocalRandom.current().nextInt(v)
}

/**
 * 随机生成整数
 *
 * @param num1 最小值
 * @param num2 最大值
 */
fun random(num1: Int, num2: Int): Int {
    val min = min(num1, num2)
    val max = max(num1, num2)
    return ThreadLocalRandom.current().nextInt(min, max + 1)
}

/**
 * 随机生成浮点数
 *
 * @param num1 最小值
 * @param num2 最大值
 */
fun random(num1: Double, num2: Double): Double {
    val min = min(num1, num2)
    val max = max(num1, num2)
    return if (min == max) max else ThreadLocalRandom.current().nextDouble(min, max)
}

/**
 * 随机生成浮点数
 *
 * @return 0-1
 */
fun randomDouble(): Double {
    return random().nextDouble()
}