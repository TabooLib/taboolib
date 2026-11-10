package taboolib.common.platform.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandBase
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CommandRegistrationConcurrencyTest {

    @Test
    fun `command tree is built once and reused by executions`() {
        val builds = AtomicInteger()
        val commandBases = CopyOnWriteArrayList<CommandBase>()
        val handlers = createCommandHandlers(false) {
            builds.incrementAndGet()
            execute(ProxyCommandSender::class.java) { _, context, _ ->
                commandBases += context.commandCompound
            }
            dynamic("value") {
                suggestionUncheck<ProxyCommandSender> { _, context ->
                    commandBases += context.commandCompound
                    listOf("value")
                }
            }
        }
        val command = command()
        val sender = TestSender("sender")

        assertTrue(handlers.executor.execute(sender, command, command.name, emptyArray()))
        assertEquals(listOf("value"), handlers.completer.execute(sender, command, command.name, arrayOf("")))

        assertEquals(1, builds.get())
        assertEquals(2, commandBases.size)
        assertSame(commandBases.first(), commandBases.last())
    }

    @Test
    fun `concurrent executions keep independent result state`() {
        val falseResultSet = CountDownLatch(1)
        val trueResultSet = CountDownLatch(1)
        val handlers = createCommandHandlers(false) {
            execute(ProxyCommandSender::class.java) { sender, context, _ ->
                if (sender.name == "false") {
                    context.commandCompound.setResult(false)
                    falseResultSet.countDown()
                    assertTrue(trueResultSet.await(5, TimeUnit.SECONDS))
                } else {
                    assertTrue(falseResultSet.await(5, TimeUnit.SECONDS))
                    context.commandCompound.setResult(true)
                    trueResultSet.countDown()
                }
            }
        }
        val command = command()
        val executor = Executors.newFixedThreadPool(2)
        try {
            val falseFuture = executor.submit<Boolean> {
                handlers.executor.execute(TestSender("false"), command, command.name, emptyArray())
            }
            val trueFuture = executor.submit<Boolean> {
                handlers.executor.execute(TestSender("true"), command, command.name, emptyArray())
            }

            assertFalse(falseFuture.get(10, TimeUnit.SECONDS))
            assertTrue(trueFuture.get(10, TimeUnit.SECONDS))
        } finally {
            trueResultSet.countDown()
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }
    }

    private fun command(): CommandStructure {
        return CommandStructure("test", emptyList(), "", "", "", "", PermissionDefault.OP, emptyMap(), false)
    }

    private class TestSender(override val name: String) : ProxyCommandSender {

        override val origin: Any
            get() = this

        override var isOp = false

        override fun isOnline() = true

        override fun sendMessage(message: String) = Unit

        override fun performCommand(command: String) = true

        override fun hasPermission(permission: String) = true
    }
}
