@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated

fun join(args: Array<String>, start: Int = 0, separator: String = " ") =
    args.joinBy(start, separator)

fun List<String>.joinBy(start: Int = 0, separator: String = " ") =
    toTypedArray().joinBy(start, separator)

fun Array<String>.joinBy(start: Int = 0, separator: String = " ") =
    filterIndexed { index, _ -> index >= start }.joinToString(separator)

fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size) =
    list.subListBy(start, end)

fun <T> List<T>.subListBy(start: Int = 0, end: Int = size) =
    filterIndexed { index, _ -> index in start until end }