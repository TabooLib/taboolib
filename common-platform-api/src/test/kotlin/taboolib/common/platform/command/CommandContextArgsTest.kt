package taboolib.common.platform.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.command.component.CommandComponentDynamic

/**
 * 新/旧解析器下 args()、get(id) 对末尾 dynamic 的合并行为
 *
 * @author sky
 */
class CommandContextArgsTest {

    private fun command(newParser: Boolean) = CommandStructure(
        "test",
        emptyList(),
        "",
        "",
        "",
        "",
        PermissionDefault.OP,
        emptyMap(),
        newParser,
    )

    private fun context(newParser: Boolean, rawArgs: Array<String>, index: Int, dynamicComment: String): CommandContext<Any?> {
        val base = CommandBase()
        val dynamic = CommandComponentDynamic(dynamicComment, "", index, false, "")
        dynamic.parent = base
        return CommandContext(
            sender = null,
            command = command(newParser),
            name = "test",
            commandCompound = base,
            newParser = newParser,
            rawArgs = rawArgs,
            index = index,
            currentComponent = dynamic,
        )
    }

    @Test
    fun `new parser merges trailing tokens for last dynamic like legacy`() {
        val ctx = context(true, arrayOf("api", "eval", "tell", "1"), 2, "script")
        assertEquals("tell 1", ctx.self())
        assertEquals("tell 1", ctx.get("script"))
    }

    @Test
    fun `legacy parser merges trailing tokens`() {
        val ctx = context(false, arrayOf("api", "eval", "tell", "1"), 2, "script")
        assertEquals("tell 1", ctx.self())
        assertEquals("tell 1", ctx.get("script"))
    }
}