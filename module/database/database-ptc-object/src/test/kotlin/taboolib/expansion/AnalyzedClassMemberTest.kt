package taboolib.expansion

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import taboolib.expansion.AnalyzedClassMember.Companion.toColumnName

class AnalyzedClassMemberTest {

    // region toColumnName

    @Test
    fun `toColumnName converts camelCase to snake_case`() {
        assertEquals("server_name", "serverName".toColumnName())
    }

    @Test
    fun `toColumnName handles leading uppercase`() {
        assertEquals("hello_world", "HelloWorld".toColumnName())
    }

    @Test
    fun `toColumnName keeps all lowercase unchanged`() {
        assertEquals("name", "name".toColumnName())
    }

    @Test
    fun `toColumnName handles consecutive uppercase`() {
        assertEquals("u_u_i_d", "UUID".toColumnName())
    }

    @Test
    fun `toColumnName handles single char`() {
        assertEquals("x", "x".toColumnName())
    }

    // endregion

    // region 类型检测

    @Test
    fun `type detection for basic types`() {
        val analyzed = AnalyzedClass.of(TypeVariety::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertTrue(memberMap["boolVal"]!!.isBoolean)
        assertTrue(memberMap["byteVal"]!!.isByte)
        assertTrue(memberMap["shortVal"]!!.isShort)
        assertTrue(memberMap["intVal"]!!.isInt)
        assertTrue(memberMap["longVal"]!!.isLong)
        assertTrue(memberMap["floatVal"]!!.isFloat)
        assertTrue(memberMap["doubleVal"]!!.isDouble)
        assertTrue(memberMap["stringVal"]!!.isString)
        assertTrue(memberMap["uuidVal"]!!.isUUID)
        assertTrue(memberMap["colorVal"]!!.isEnum)
    }

    @Test
    fun `canConvertedInteger includes boolean byte short int long char`() {
        val analyzed = AnalyzedClass.of(TypeVariety::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertTrue(memberMap["boolVal"]!!.canConvertedInteger())
        assertTrue(memberMap["byteVal"]!!.canConvertedInteger())
        assertTrue(memberMap["shortVal"]!!.canConvertedInteger())
        assertTrue(memberMap["intVal"]!!.canConvertedInteger())
        assertTrue(memberMap["longVal"]!!.canConvertedInteger())
        assertFalse(memberMap["floatVal"]!!.canConvertedInteger())
        assertFalse(memberMap["doubleVal"]!!.canConvertedInteger())
    }

    @Test
    fun `canConvertedDecimal includes float and double`() {
        val analyzed = AnalyzedClass.of(TypeVariety::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertTrue(memberMap["floatVal"]!!.canConvertedDecimal())
        assertTrue(memberMap["doubleVal"]!!.canConvertedDecimal())
        assertFalse(memberMap["intVal"]!!.canConvertedDecimal())
    }

    @Test
    fun `canConvertedString includes string enum uuid`() {
        val analyzed = AnalyzedClass.of(TypeVariety::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertTrue(memberMap["stringVal"]!!.canConvertedString())
        assertTrue(memberMap["uuidVal"]!!.canConvertedString())
        assertTrue(memberMap["colorVal"]!!.canConvertedString())
        assertFalse(memberMap["intVal"]!!.canConvertedString())
    }

    // endregion

    // region 注解识别

    @Test
    fun `annotation recognition`() {
        val analyzed = AnalyzedClass.of(AnnotatedData::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertTrue(memberMap["pk"]!!.isPrimary)
        assertFalse(memberMap["idx"]!!.isPrimary)

        assertTrue(memberMap["idx"]!!.isKey)
        assertFalse(memberMap["pk"]!!.isKey)

        assertTrue(memberMap["uniq"]!!.isUniqueKey)
        assertFalse(memberMap["pk"]!!.isUniqueKey)

        assertTrue(memberMap["required"]!!.isNotNull)
        assertFalse(memberMap["pk"]!!.isNotNull)

        assertEquals(128, memberMap["long128"]!!.length)
        assertEquals(64, memberMap["pk"]!!.length) // 默认值
    }

    @Test
    fun `alias annotation overrides column name`() {
        val analyzed = AnalyzedClass.of(AnnotatedData::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertEquals("custom_col", memberMap["aliased"]!!.name)
        assertEquals("aliased", memberMap["aliased"]!!.propertyName)
    }

    // endregion

    // region isFinal

    @Test
    fun `isFinal detects val vs var`() {
        val analyzed = AnalyzedClass.of(AnnotatedData::class.java)
        val memberMap = analyzed.members.associateBy { it.propertyName }

        assertTrue(memberMap["pk"]!!.isFinal)
        assertFalse(memberMap["mutable"]!!.isFinal)
    }

    // endregion

    // region Fix 7: toColumnName 前导下划线修复

    @Test
    fun `toColumnName no leading underscore for PascalCase`() {
        assertEquals("player_home", "PlayerHome".toColumnName())
    }

    @Test
    fun `toColumnName no leading underscore for single uppercase`() {
        assertEquals("a", "A".toColumnName())
    }

    // endregion
}
