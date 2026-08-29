package taboolib.module.lang

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class LanguageBoundaryTest {

    @Test
    fun `json normalization preserves nested value types and nulls`() {
        val normalized = normalizeJsonValue(
            mapOf(
                "object" to mapOf("enabled" to true, "count" to 3),
                "array" to listOf(1, false, mapOf("nested" to 2.5)),
                "nullable" to null,
            )
        ) as Map<*, *>

        val objectValue = normalized["object"] as Map<*, *>
        val arrayValue = normalized["array"] as List<*>
        assertSame(true, objectValue["enabled"])
        assertEquals(3, objectValue["count"])
        assertEquals(1, arrayValue[0])
        assertSame(false, arrayValue[1])
        assertEquals(2.5, (arrayValue[2] as Map<*, *>)["nested"])
        assertTrue(normalized.containsKey("nullable"))
        assertNull(normalized["nullable"])
    }

    @Test
    fun `type json reinitialization replaces old arguments`() {
        val type = TypeJson()
        type.init(mapOf("text" to "[value]", "args" to listOf(mapOf("type" to "text", "old" to 1))))
        type.init(mapOf("text" to "[value]", "args" to listOf(mapOf("type" to "translate:1:Stone", "new" to true))))

        assertEquals(1, type.jsonArgs.size)
        assertFalse(type.jsonArgs.single().containsKey("old"))
        assertSame(true, type.jsonArgs.single()["new"])
    }

    @Test
    fun `json type parser separates translate arguments without prefix matches`() {
        assertEquals("translate" to emptyList<String>(), parseJsonType("translate"))
        assertEquals("translate" to listOf("1", "Stone"), parseJsonType("translate:1:Stone"))
        assertEquals("translateFoo" to emptyList<String>(), parseJsonType("translateFoo"))
    }

    @Test
    fun `language resource lookup uses exact code and supported extension`() {
        val resources = setOf("lang/en_US.yml", "lang/en_GB.json", "lang/en.txt", "other/en.yml")
        assertEquals("lang/en_US.yml", findLanguageResource(resources, "lang", "en_US") { it == "yml" || it == "json" })
        assertNull(findLanguageResource(resources, "lang", "en") { it == "yml" || it == "json" })
        assertEquals("lang/en_GB.json", findLanguageResource(resources, "lang", "en_GB") { it == "yml" || it == "json" })
    }

    @Test
    fun `language cache replacement preserves public map reference`() {
        val originalFile = LanguageFile(File("old.yml"), hashMapOf())
        val replacementFile = LanguageFile(File("new.yml"), hashMapOf())
        val target: HashMap<String, LanguageFile> = SnapshotHashMap(mapOf("old" to originalFile))
        val exposed = target
        val keys = target.keys

        replaceLanguageFiles(target, mapOf("new" to replacementFile))

        assertSame(exposed, target)
        assertFalse(target.containsKey("old"))
        assertTrue(keys.contains("new"))
        assertSame(replacementFile, target["new"])
        target.entries.single().setValue(originalFile)
        assertSame(originalFile, target["new"])
        target["new"] = replacementFile
        assertTrue(target.values.remove(replacementFile))
        assertTrue(target.isEmpty())
    }

    @Test
    fun `language file replaces complete node snapshot`() {
        val original = hashMapOf<String, Type>("old" to TypeText("old"))
        val languageFile = LanguageFile(File("unused.yml"), original)
        val replacement = hashMapOf<String, Type>("new" to TypeText("new"))
        val exposed = languageFile.nodes

        languageFile.replaceNodes(replacement)

        assertSame(exposed, languageFile.nodes)
        assertFalse(languageFile.nodes.containsKey("old"))
        assertTrue(languageFile.nodes.containsKey("new"))
    }
}
