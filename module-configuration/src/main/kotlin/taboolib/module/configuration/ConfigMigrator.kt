@file:Isolated

package taboolib.module.configuration

import taboolib.common.Isolated
import taboolib.library.configuration.MemorySection
import java.io.InputStream
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
fun Map<String, Any>.flatten(): Map<String, Any> {
    val flatMap = TreeMap<String, Any>()
    forEach { (k, v) ->
        if (v is Map<*, *>) {
            flatMap += (v as Map<String, Any>).flatten().mapKeys { "$k.${it.key}" }
        } else {
            flatMap[k] = v
        }
    }
    return flatMap
}

/**
 * 计算 Map 差异（源 -> 目标）
 */
@Suppress("UNCHECKED_CAST")
fun Map<String, Any>.contrastAs(target: Map<String, Any>): Set<Update> {
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

/**
 * 更新配置文件（源 -> 目标）
 */
fun InputStream.migrateTo(target: InputStream): ByteArray? {
    var migrated = false
    var append = false
    val contextSource = readBytes().toString(StandardCharsets.UTF_8)
    val contextTarget = target.readBytes().toString(StandardCharsets.UTF_8)
    val hashSource = contextSource.hash("sha-1")
    var hashTarget = ""
    contextTarget.lines().forEach {
        if (it.startsWith("# HASH ")) {
            hashTarget = it.substring("# HASH ".length).trim().split(" ")[0]
        }
    }
    if (hashSource == hashTarget) {
        return null
    }
    val configSource = SecuredFile.loadConfiguration(contextSource)
    val configTarget = SecuredFile.loadConfiguration(contextTarget)
    val csv = configSource.getValues(true).filterValues { it !is MemorySection }
    val ctv = configTarget.getValues(true).filterValues { it !is MemorySection }
    val update = csv.contrastAs(ctv).filter { it.type == Update.Type.ADD }

    fun readCommits(node: String): List<String> {
        val commits = ArrayList<String>()
        contextSource.lines().map { it.trim() }.forEach {
            when {
                it.startsWith('#') -> {
                    commits += it.substring(1)
                }
                it.isEmpty() -> {
                    commits.clear()
                }
                it.endsWith(':') -> {
                    val key = it.substring(0, it.length - 1).split('.')

                }
            }
        }
        return commits
    }


    return null
}

fun String.hash(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    digest.update(toByteArray(StandardCharsets.UTF_8))
    return BigInteger(1, digest.digest()).toString(16)
}

@Isolated
class Update(val type: Type, val node: String, val value: Any?) : Comparable<Update> {

    enum class Type {

        ADD, MODIFY, DELETE
    }

    override fun toString(): String {
        return "Update(type=$type, node='$node', value=$value)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Update) return false
        if (node != other.node) return false
        return true
    }

    override fun hashCode(): Int {
        return node.hashCode()
    }

    override fun compareTo(other: Update): Int {
        return node.compareTo(other.node)
    }
}