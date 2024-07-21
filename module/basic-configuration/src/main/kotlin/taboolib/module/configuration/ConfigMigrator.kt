package taboolib.module.configuration

import java.util.*

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.flatten(): Map<String, Any?> {
    val flatMap = TreeMap<String, Any?>()
    forEach { (k, v) ->
        if (v is Map<*, *>) {
            flatMap += (v as Map<String, Any?>).flatten().mapKeys { "$k.${it.key}" }
        } else {
            flatMap[k] = v
        }
    }
    return flatMap
}

/**
 * 计算 Map 差异（源 -> 目标）
 */
fun Map<String, Any?>.contrastAs(target: Map<String, Any?>): Set<Update> {
    val update = TreeSet<Update>()
    val sourceMap = flatten()
    val targetMap = target.flatten()
    targetMap.filter { it.key !in sourceMap }.forEach { update += Update(Update.Type.DELETE, it.key, it.value) }
    sourceMap.forEach { (k, v) ->
        if (k !in targetMap) {
            update += Update(Update.Type.ADD, k, v)
        } else if (v != targetMap[k]) {
            update += Update(Update.Type.MODIFY, k, v)
        }
    }
    return update
}

class Update(val type: Type, val node: String, val value: Any?) : Comparable<Update> {

    enum class Type {

        ADD, MODIFY, DELETE
    }

    override fun compareTo(other: Update): Int {
        return node.compareTo(other.node)
    }
}