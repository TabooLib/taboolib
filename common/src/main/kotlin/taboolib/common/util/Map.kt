@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common.util

import taboolib.common.Isolated

inline fun <K, V> subMap(map: Map<K, V>, start: Int = 0, end: Int = map.size - 1): List<Map.Entry<K, V>> {
    return map.entries.filterIndexed { index, _ -> index in start..end }
}