@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated

fun <K, V> subMap(map: Map<K, V>, start: Int = 0, end: Int = map.size - 1) =
    map.subMapBy(start, end)

fun <K, V> Map<K, V>.subMapBy(start: Int = 0, end: Int = size - 1) =
    entries.filterIndexed { index, _ -> index in start..end }