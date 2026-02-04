package taboolib.expansion

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnalyzedClassTest {

    @BeforeEach
    fun clearCache() {
        AnalyzedClass.cached.clear()
    }

    @Test
    fun `members correctly parsed from data class`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        assertEquals(3, analyzed.members.size)
        assertEquals("name", analyzed.members[0].name)
        assertEquals("value", analyzed.members[1].name)
        assertEquals("description", analyzed.members[2].name)
    }

    @Test
    fun `primary member identified by @Id`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        assertNotNull(analyzed.primaryMember)
        assertEquals("name", analyzed.primaryMemberName)
    }

    @Test
    fun `isFinal distinguishes val and var`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertTrue(memberMap["name"]!!.isFinal)   // val
        assertFalse(memberMap["value"]!!.isFinal)  // var
        assertFalse(memberMap["description"]!!.isFinal) // var
    }

    @Test
    fun `createInstance creates object from map`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        val map = mapOf("name" to "test", "value" to 42, "description" to "desc")
        val instance = analyzed.createInstance<SimpleData>(map)
        assertEquals("test", instance.name)
        assertEquals(42, instance.value)
        assertEquals("desc", instance.description)
    }

    @Test
    fun `createInstance with missing map key passes null`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        // 缺失的 key 会导致 map[key] 返回 null，传入构造器
        // SimpleData 的 description 是非空 String，传 null 会导致构造器异常
        val map = mapOf("name" to "test", "value" to 10)
        assertThrows(IllegalStateException::class.java) {
            analyzed.createInstance<SimpleData>(map)
        }
    }

    @Test
    fun `companion wrapper function is detected`() {
        val analyzed = AnalyzedClass.of(WrapperData::class.java)
        assertNotNull(analyzed.wrapperObjectInstance)
        assertNotNull(analyzed.wrapperFunction)
    }

    @Test
    fun `createInstance uses wrapper function when available`() {
        val analyzed = AnalyzedClass.of(WrapperData::class.java)
        val map = mapOf("id" to "w1", "count" to 99)
        val instance = analyzed.createInstance<WrapperData>(map)
        assertEquals("w1", instance.id)
        assertEquals(99, instance.count)
    }

    @Test
    fun `no wrapper function for simple data class`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        assertNull(analyzed.wrapperFunction)
    }

    @Test
    fun `getPrimaryMemberValue returns @Id field value`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        val data = SimpleData("mykey", 10, "hello")
        assertEquals("mykey", analyzed.getPrimaryMemberValue(data))
    }

    @Test
    fun `getValue returns member value`() {
        val analyzed = AnalyzedClass.of(SimpleData::class.java)
        val data = SimpleData("k", 77, "desc")
        val valueMember = analyzed.members.first { it.propertyName == "value" }
        assertEquals(77, analyzed.getValue(data, valueMember))
    }

    @Test
    fun `multiple @Id throws error`() {
        // MultipleIdData 有两个 @Id，应该抛异常
        assertThrows(IllegalStateException::class.java) {
            AnalyzedClass.of(MultipleIdData::class.java)
        }
    }

    @Test
    fun `all val data class has no mutable fields`() {
        val analyzed = AnalyzedClass.of(AllValData::class.java)
        assertTrue(analyzed.members.all { it.isFinal })
    }

    @Test
    fun `cached instances are reused`() {
        val a = AnalyzedClass.of(SimpleData::class.java)
        val b = AnalyzedClass.of(SimpleData::class.java)
        assertSame(a, b)
    }

    // region Fix 1: 同类型多字段按名称匹配

    @Test
    fun `dual string fields matched by name not type`() {
        val analyzed = AnalyzedClass.of(DualStringData::class.java)
        assertEquals(2, analyzed.members.size)
        // 按名称匹配，确保 first/second 不错配
        val data = DualStringData("hello", "world")
        assertEquals("hello", analyzed.getPrimaryMemberValue(data))
        val secondMember = analyzed.members.first { it.propertyName == "second" }
        assertEquals("world", analyzed.getValue(data, secondMember))
    }

    // endregion

    // region Fix 2: Enum 反序列化容错

    @Test
    fun `enum createInstance with actual enum values`() {
        val analyzed = AnalyzedClass.of(EnumData::class.java)
        val map = mapOf<String, Any?>("id" to "e1", "color" to Color.RED, "status" to Status.ACTIVE)
        val instance = analyzed.createInstance<EnumData>(map)
        assertEquals("e1", instance.id)
        assertEquals(Color.RED, instance.color)
        assertEquals(Status.ACTIVE, instance.status)
    }

    // endregion

    // region Fix 3: null 值进入 map

    @Test
    fun `read null values into map for nullable fields`() {
        // 验证 createInstance 可以处理 null 值
        val analyzed = AnalyzedClass.of(NullableData::class.java)
        val map = mapOf<String, Any?>("id" to "n1", "label" to null, "note" to null)
        val instance = analyzed.createInstance<NullableData>(map)
        assertEquals("n1", instance.id)
        assertNull(instance.label)
        assertNull(instance.note)
    }

    // endregion

    // region Fix 4: getValue 返回 nullable

    @Test
    fun `getValue returns null for nullable field`() {
        val analyzed = AnalyzedClass.of(NullableData::class.java)
        val data = NullableData("n1", null, null)
        val labelMember = analyzed.members.first { it.propertyName == "label" }
        assertNull(analyzed.getValue(data, labelMember))
    }

    // endregion
}

/** 用于测试多个 @Id 场景 */
private data class MultipleIdData(
    @Id val id1: String,
    @Id val id2: String,
    var value: Int
)
