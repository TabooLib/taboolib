package taboolib.platform

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BukkitCommandRegistryTest {

    @Test
    fun `matches primary aliases and own namespace`() {
        assertTrue(commandLabelMatches("main", listOf("alias"), "main", "plugin"))
        assertTrue(commandLabelMatches("main", listOf("alias"), "alias", "plugin"))
        assertTrue(commandLabelMatches("main", listOf("alias"), "plugin:main", "plugin"))
        assertTrue(commandLabelMatches("main", listOf("alias"), "plugin:alias", "plugin"))
        assertTrue(commandLabelMatches("main", listOf("alias"), "PLUGIN:MAIN", "plugin"))
        assertTrue(commandLabelMatches("main", listOf("alias"), "ALIAS", "plugin"))
        assertFalse(commandLabelMatches("main", listOf("alias"), "other:main", "plugin"))
        assertFalse(commandLabelMatches("main", listOf("alias"), "missing", "plugin"))
    }

    @Test
    fun `re-registration cleanup removes old and new aliases by identity`() {
        val old = Any()
        val replacement = Any()
        val commands = linkedMapOf(
            "main" to old,
            "old-alias" to old,
            "plugin:main" to old,
        )

        assertTrue(removeMappingsByIdentity(commands, old))
        commands["main"] = replacement
        commands["new-alias"] = replacement
        commands["plugin:main"] = replacement
        assertFalse(commands.containsKey("old-alias"))

        assertTrue(removeMappingsByIdentity(commands, replacement))
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `removes every mapping for the same command instance`() {
        val target = Any()
        val other = Any()
        val commands = linkedMapOf(
            "main" to target,
            "alias" to target,
            "plugin:main" to target,
            "other" to other,
        )

        assertTrue(removeMappingsByIdentity(commands, target))
        assertSame(other, commands["other"])
        assertFalse(commands.containsKey("main"))
        assertFalse(commands.containsKey("alias"))
        assertFalse(commands.containsKey("plugin:main"))
        assertFalse(removeMappingsByIdentity(commands, target))
    }
}
