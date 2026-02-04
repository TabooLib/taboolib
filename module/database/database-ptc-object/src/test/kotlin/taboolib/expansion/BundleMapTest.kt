package taboolib.expansion

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BundleMapTest {

    @Test
    fun `get returns value by name`() {
        val bundle = BundleMapImpl(mapOf("name" to "sky", "age" to 25))
        assertEquals("sky", bundle.get<String>("name"))
        assertEquals(25, bundle.get<Int>("age"))
    }

    @Test
    fun `getOrNull returns value when exists`() {
        val bundle = BundleMapImpl(mapOf("key" to "val"))
        assertEquals("val", bundle.getOrNull<String>("key"))
    }

    @Test
    fun `getOrNull returns null when key missing`() {
        val bundle = BundleMapImpl(mapOf("key" to "val"))
        assertNull(bundle.getOrNull<String>("missing"))
    }

    @Test
    fun `get returns null for existing key with null value`() {
        val bundle = BundleMapImpl(mapOf("key" to null))
        assertNull(bundle.get<String?>("key"))
    }

    @Test
    fun `getOrNull returns null for existing key with null value`() {
        val bundle = BundleMapImpl(mapOf("key" to null))
        // containsKey("key") 为 true，所以走 get 分支，返回 null
        assertNull(bundle.getOrNull<String>("key"))
    }

    @Test
    fun `empty map returns null for get`() {
        val bundle = BundleMapImpl(emptyMap())
        assertNull(bundle.get<String?>("anything"))
    }

    @Test
    fun `unchecked cast behavior`() {
        val bundle = BundleMapImpl(mapOf("num" to 42))
        // Int 被 unchecked cast 为 Any
        val value: Any = bundle.get("num")
        assertEquals(42, value)
    }
}
