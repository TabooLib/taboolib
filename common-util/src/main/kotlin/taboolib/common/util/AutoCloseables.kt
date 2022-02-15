package taboolib.common.util

import java.io.Closeable

inline fun <T> using(block: ResourceHolder.() -> T): T {
    ResourceHolder().use { return it.block() }
}

class ResourceHolder : AutoCloseable, Closeable {
    private val resources = mutableListOf<Any>()

    fun <T : AutoCloseable> T.cage(): T {
        resources += this
        return this
    }

    fun <T : Closeable> T.cage(): T {
        resources += this
        return this
    }

    override fun close() {
        val source = resources.reversed()
        source.filterIsInstance<AutoCloseable>().forEach { it.close() }
        source.filterIsInstance<Closeable>().forEach { it.close() }
    }
}
