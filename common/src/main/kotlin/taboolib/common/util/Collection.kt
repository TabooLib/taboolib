@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated

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