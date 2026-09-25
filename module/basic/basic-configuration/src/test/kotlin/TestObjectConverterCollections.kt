import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.conversion.ObjectConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.util.EnumSet
import java.util.LinkedHashSet
import java.util.LinkedList
import java.util.Queue
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

class TestObjectConverterCollections {

    @Test
    fun `preserves declared collection types and converts nested elements`() {
        val root = Config.inMemory()
        root.set<Any>("names", listOf("alpha", "beta", "alpha"))
        root.set<Any>("queue", listOf("first", "second"))
        root.set<Any>("numbers", listOf(1L, 2L))
        root.set<Any>("sorted", listOf("beta", "alpha"))
        root.set<Any>("modes", listOf("first", "SECOND"))
        root.set<Any>("groups", listOf(listOf(itemConfig("one"), itemConfig("two"))))

        val indexed = Config.inMemory()
        indexed.set<Any>("primary", itemConfig("indexed"))
        root.set<Any>("indexed", indexed)

        val sortedIndex = Config.inMemory()
        sortedIndex.set<Any>("second", 2)
        sortedIndex.set<Any>("first", 1)
        root.set<Any>("sortedIndex", sortedIndex)

        val result = CollectionHolder()
        ObjectConverter().toObject(root, result)

        assertInstanceOf(LinkedHashSet::class.java, result.names)
        assertEquals(linkedSetOf("alpha", "beta"), result.names)
        assertInstanceOf(LinkedList::class.java, result.queue)
        assertEquals(listOf("first", "second"), result.queue.toList())
        assertInstanceOf(LinkedList::class.java, result.numbers)
        assertEquals(listOf(1, 2), result.numbers)
        assertInstanceOf(TreeSet::class.java, result.sorted)
        assertEquals(listOf("alpha", "beta"), result.sorted.toList())
        assertEquals(EnumSet.of(Mode.FIRST, Mode.SECOND), result.modes)
        assertInstanceOf(LinkedHashSet::class.java, result.groups.single())
        assertEquals(listOf("one", "two"), result.groups.single().map { it.name })
        assertEquals("indexed", result.indexed.getValue("primary").name)
        assertInstanceOf(TreeMap::class.java, result.sortedIndex)
        assertEquals(listOf("first", "second"), result.sortedIndex.keys.toList())
        assertEquals(listOf(1L, 2L), result.sortedIndex.values.toList())
    }

    private fun itemConfig(name: String): Config {
        return Config.inMemory().also { it.set<Any>("name", name) }
    }

    class CollectionHolder {

        var names: Set<String> = emptySet()
        var queue: Queue<String> = LinkedList()
        var numbers: LinkedList<Int> = LinkedList()
        var sorted: SortedSet<String> = sortedSetOf()
        var modes: EnumSet<Mode> = EnumSet.noneOf(Mode::class.java)
        var groups: List<Set<Item>> = emptyList()
        var indexed: Map<String, Item> = emptyMap()
        var sortedIndex: SortedMap<String, Long> = sortedMapOf()
    }

    class Item {

        var name: String = ""
    }

    enum class Mode {
        FIRST,
        SECOND,
    }
}
