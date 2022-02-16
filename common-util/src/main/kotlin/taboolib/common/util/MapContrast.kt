@file:Isolated

package taboolib.common.util

import taboolib.common.Isolated
import java.util.*

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.flatten(): Map<String, Any?> {
    val flatMap = TreeMap<String, Any?>()

    forEach { (k, v) ->
        if (v is Map<*, *>) {
            (v as Map<String, Any?>).flatten().mapKeys { "$k.${it.key}" }.let { flatMap += it }
        } else {
            flatMap[k] = v
        }
    }
    return flatMap
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.contrastAs(target: Map<String, Any?>): Set<Difference> {
    val update = TreeSet<Difference>()
    val sourceMap = flatten()
    val targetMap = target.flatten()

    targetMap.filter { it.key !in sourceMap }.forEach { update += Difference(Difference.Type.DELETE, it.key, it.value) }
    sourceMap.forEach { (k, v) ->
        if (k !in targetMap) {
            update += Difference(Difference.Type.ADD, k, v)
        } else if (v != targetMap[k]) {
            update += Difference(Difference.Type.MODIFY, k, v)
        }
    }
    return update
}

@Isolated
class Difference(val type: Type, val node: String, val value: Any?) : Comparable<Difference> {

    enum class Type {

        ADD, MODIFY, DELETE
    }

    override fun compareTo(other: Difference) = node.compareTo(other.node)
}
