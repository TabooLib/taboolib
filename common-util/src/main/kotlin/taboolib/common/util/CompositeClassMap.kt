package taboolib.common.util

import org.tabooproject.reflex.ReflexClass
import java.util.AbstractMap

/**
 * 组合多个 Map 的视图，不触发底层 Map 的全量遍历
 * 用于保持 LazyReflexClassMap 的懒加载特性
 */
class CompositeClassMap(private val maps: List<Map<String, ReflexClass>>) : AbstractMap<String, ReflexClass>() {

    override val size: Int
        get() = maps.sumOf { it.size }

    override fun containsKey(key: String): Boolean = maps.any { it.containsKey(key) }

    override fun get(key: String): ReflexClass? {
        for (map in maps) {
            map[key]?.let { return it }
        }
        return null
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, ReflexClass>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<String, ReflexClass>>() {

            override val size: Int get() = this@CompositeClassMap.size

            override fun add(element: MutableMap.MutableEntry<String, ReflexClass>): Boolean {
                throw UnsupportedOperationException("Read-only map")
            }

            override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, ReflexClass>> {
                val baseIterator = maps.asSequence().flatMap { it.entries.asSequence() }.iterator()
                return object : MutableIterator<MutableMap.MutableEntry<String, ReflexClass>> {

                    override fun hasNext() = baseIterator.hasNext()

                    override fun next(): MutableMap.MutableEntry<String, ReflexClass> {
                        val entry = baseIterator.next()
                        // 包装成 MutableEntry
                        return object : MutableMap.MutableEntry<String, ReflexClass> {

                            override val key: String get() = entry.key

                            override val value: ReflexClass get() = entry.value

                            override fun setValue(newValue: ReflexClass): ReflexClass {
                                throw UnsupportedOperationException("Read-only map")
                            }
                        }
                    }

                    override fun remove() = throw UnsupportedOperationException("Read-only map")
                }
            }
        }
}