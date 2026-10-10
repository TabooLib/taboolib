package taboolib.common.platform.command

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentLiteral

class SimpleCommandTest {

    @Test
    fun `empty body tree still applies body function`() {
        val body = SimpleCommandBody {
            literal("declared")
        }.apply {
            name = "root"
        }
        val command = CommandBase()

        register(body, command)

        val root = command.children.single() as CommandComponentLiteral
        assertArrayEquals(arrayOf("root"), root.aliases)
        assertArrayEquals(arrayOf("declared"), (root.children.single() as CommandComponentLiteral).aliases)
    }

    @Test
    fun `body function and nested bodies register consistently`() {
        val body = SimpleCommandBody {
            literal("declared")
        }.apply {
            name = "root"
            children += SimpleCommandBody {
                literal("leaf")
            }.apply {
                name = "nested"
            }
        }
        val command = CommandBase()

        register(body, command)

        val root = command.children.single() as CommandComponentLiteral
        assertEquals(2, root.children.size)
        assertArrayEquals(arrayOf("declared"), (root.children[0] as CommandComponentLiteral).aliases)
        val nested = root.children[1] as CommandComponentLiteral
        assertArrayEquals(arrayOf("nested"), nested.aliases)
        assertArrayEquals(arrayOf("leaf"), (nested.children.single() as CommandComponentLiteral).aliases)
    }

    private fun register(body: SimpleCommandBody, component: CommandComponent) {
        val method = Class.forName("taboolib.common.platform.command.SimpleCommandKt")
            .getDeclaredMethod("registerTo", SimpleCommandBody::class.java, CommandComponent::class.java)
        method.isAccessible = true
        method.invoke(null, body, component)
    }
}
