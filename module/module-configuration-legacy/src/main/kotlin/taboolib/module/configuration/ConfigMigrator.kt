package taboolib.module.configuration

import taboolib.common.io.digest
import taboolib.common.util.addSafely
import taboolib.common.util.each
import taboolib.library.configuration.MemorySection
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*

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
    val contextSource = readBytes().toString(StandardCharsets.UTF_8)
    var contextTarget = target.readBytes().toString(StandardCharsets.UTF_8)
    val hashSource = contextSource.digest("sha-1")
    var hashTarget = ""
    contextTarget.lines().forEach {
        if (it.startsWith("# VERSION ")) {
            hashTarget = it.substring("# VERSION ".length).trim().split(" ")[0]
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
    if (update.isEmpty()) {
        return null
    }
    val updateGroup = HashMap<String, MutableList<Update>>()
    update.forEach {
        val groupKey = updateGroup.keys.firstOrNull { key -> it.node.parentNode().startsWith(key) }
        if (groupKey != null) {
            updateGroup[groupKey]!! += it
        } else {
            updateGroup[it.node.parentNode()] = arrayListOf(it)
        }
    }
    val sourceLines = contextSource.lines()
    fun find(node: String): List<String> {
        val sourceNode = ConfigFinder.findNode(node, contextSource)
        val spaceCount = sourceLines[sourceNode.line].spaceCount()
        val source = sourceNode.commits.map { c -> "${" ".repeat(spaceCount)}#$c" }.toMutableList()
        sourceLines.each(start = sourceNode.line) { index, s ->
            if (index == sourceNode.line || s.spaceCount() > spaceCount) {
                source += s
            } else {
                close()
            }
        }
        return source
    }
    updateGroup.forEach { (parent, updates) ->
        if (ConfigFinder.findNode(parent, contextTarget).line == -1) {
            contextTarget += "\n${find(parent).joinToString("\n")}"
        } else {
            updates.forEach { update ->
                val mul = contextTarget.lines().toMutableList()
                val ins = ConfigFinder.findNode(update.node.parentNode(), contextTarget).line
                if (ins != -1) {
                    mul.addSafely(ins + 1, find(update.node).joinToString("\n"), "")
                    contextTarget = mul.joinToString("\n")
                }
                // FIXME: 2021/7/13 多层节点错误 [Update(node=c.3.1), Update(node=c.3.2)]
            }
        }
    }
    if (hashTarget.isEmpty()) {
        contextTarget += listOf(
            "",
            "",
            "# ------------------------------------------------ #",
            "# VERSION $hashSource #",
            "# ------------------------------------------------ #",
        ).joinToString("\n")
    } else {
        val mul = contextTarget.lines().toMutableList()
        mul.forEachIndexed { index, s ->
            if (s.startsWith("# VERSION ")) {
                mul[index] = "# VERSION $hashSource #"
            }
        }
        contextTarget = mul.joinToString("\n")
    }
    return contextTarget.toByteArray(StandardCharsets.UTF_8)
}

private fun String.parentNode(): String {
    val lastIndexOf = lastIndexOf('.')
    return if (lastIndexOf != -1) substring(0, lastIndexOf) else this
}

private fun String.spaceCount(): Int {
    var i = 0
    for (it in this) {
        if (it == ' ') {
            i++
        } else {
            break
        }
    }
    return i
}

object ConfigFinder {

    data class Result(val line: Int, val commits: List<String>)

    fun findNode(node: String, context: String): Result {
        val nodes = node.split('.')
        val lines = context.lines()
        var space = 0
        var find = -1
        fun find(cur: Int): Int {
            if (cur < nodes.size) {
                val r = lines.each(start = find) { index, it ->
                    val spaceCount = it.spaceCount()
                    if (spaceCount == space) {
                        val trim = it.trim()
                        if (trim.matches("(['\"])?(${nodes[cur]})(['\"])?:(.*)".toRegex())) {
                            space = spaceCount + 2
                            find = index
                            return@each find(cur + 1)
                        }
                    }
                    null
                }
                if (r != null) {
                    return r
                }
            } else {
                return find
            }
            return -1
        }

        fun findCommits(line: Int): List<String> {
            return LinkedList<String>().also { list ->
                lines.each(start = line, reversed = true) { _, it ->
                    if (it.trim().startsWith('#')) {
                        list.addFirst(it.trim().substring(1))
                    } else {
                        close()
                    }
                }
            }
        }

        val line = find(0)
        return Result(line, findCommits(line))
    }
}

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