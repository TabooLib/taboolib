package taboolib.common.util

fun join(args: Array<String>, start: Int = 0, separator: String = " "): String {
    return args.filterIndexed { index, _ -> index >= start }.joinToString(separator)
}

/**
 * 获取列表中特定范围内的元素
 *
 * @param list 列表
 * @param start 开始位置
 * @param end 结束位置（默认为元素数量）
 */
fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size): List<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}

fun Any.asList(): List<String> {
    return when (this) {
        is Collection<*> -> map { it.toString() }
        is Array<*> -> map { it.toString() }
        else -> listOf(toString())
    }
}

/**
 * 安全的写入元素
 *
 * @param index 下标
 * @param element 元素
 * @param def 若写入位置之前存在空缺，则写入该默认值
 */
fun <T> MutableList<T>.setSafely(index: Int, element: T, def: T) {
    while (index >= size) {
        add(def)
    }
    this[index] = element
}

/**
 * 安全的插入元素
 *
 * @param index 下标
 * @param element 元素
 * @param def 若写入位置之前存在空缺，则写入该默认值
 */
fun <T> MutableList<T>.addSafely(index: Int, element: T, def: T) {
    while (index >= size) {
        add(def)
    }
    add(index, element)
}

/**
 *
 * # 根据条件删除元素并返回
 *
 * @param con 匹配条件
 * @return 返回被删除的元素
 *
 * @author Neon -老廖
 */
inline fun <T> MutableCollection<T>.removeAndBackIf(con: (T) -> Boolean): List<T> {
    if (isEmpty()) {
        return emptyList()
    }
    // 没有匹配元素时应该返回更轻的空列表
    var b: MutableList<T>? = null
    val i = this.iterator()
    while (i.hasNext()) {
        val a = i.next()
        if (a != null && con(a)) {
            if (b == null) {
                b = mutableListOf()
            }
            b.add(a)
            i.remove()
        }
    }
    return b ?: emptyList()
}