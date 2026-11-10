package taboolib.platform

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BungeeCompatibilityTest {

    @Test
    fun `registered command keeps configured aliases`() {
        val command = registeredCommand(listOf("alias", "short"))

        assertEquals("main", command.name)
        assertEquals("plugin.command.main", command.permission)
        assertArrayEquals(arrayOf("alias", "short"), command.aliases)
    }

    @Test
    fun `title components keep title and subtitle independent`() {
        val (title, subtitle) = titleComponents("Title", "Subtitle")
        assertEquals("Title", title.text)
        assertEquals("Subtitle", subtitle.text)

        val (emptyTitle, emptySubtitle) = titleComponents(null, null)
        assertEquals("", emptyTitle.text)
        assertEquals("", emptySubtitle.text)
    }

    private fun registeredCommand(aliases: List<String>): Command {
        val constructor = Class.forName("taboolib.platform.RegisteredBungeeCommand").declaredConstructors.single()
        constructor.isAccessible = true
        val execute: (CommandSender, Array<String>) -> Unit = { _, _ -> }
        val complete: (CommandSender, Array<String>) -> MutableIterable<String> = { _, _ -> mutableListOf() }
        return constructor.newInstance("main", "plugin.command.main", aliases, execute, complete) as Command
    }

    @Suppress("UNCHECKED_CAST")
    private fun titleComponents(title: String?, subtitle: String?): Pair<TextComponent, TextComponent> {
        val method = Class.forName("taboolib.platform.type.BungeePlayerKt")
            .getDeclaredMethod("bungeeTitleComponents", String::class.java, String::class.java)
        method.isAccessible = true
        return method.invoke(null, title, subtitle) as Pair<TextComponent, TextComponent>
    }
}
