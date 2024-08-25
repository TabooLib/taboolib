package taboolib.common.util

/**
 * 执行给定的代码块，并返回所用时间（以毫秒为单位）。
 *
 * @param task 要执行的代码块
 * @return 执行代码块所用的时间（以毫秒为单位）
 */
inline fun <T> execution(crossinline task: () -> T): Pair<T, Long> {
    val startTime = System.nanoTime()
    // 执行传入的代码块并获取结果
    val result = task()
    val endTime = System.nanoTime()
    // 计算执行时间（以毫秒为单位）
    val duration = (endTime - startTime) / 1_000_000
    return Pair(result, duration)
}