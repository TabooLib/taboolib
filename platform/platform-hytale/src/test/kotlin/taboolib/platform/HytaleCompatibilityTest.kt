package taboolib.platform

import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.command.system.ParseResult
import com.hypixel.hytale.server.core.command.system.ParserContext
import com.hypixel.hytale.server.core.command.system.Tokenizer
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.command.PermissionDefault
import taboolib.platform.type.HytaleCommandSender
import java.lang.reflect.Proxy
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function

class HytaleCompatibilityTest {

    @Test
    fun `command keeps explicit permission and leaves empty permission native`() {
        assertEquals("plugin.command.root", commandPermission("plugin.command.root"))
        assertEquals(null, commandPermission(""))
    }

    @Test
    fun `native first positional argument owns completer and zero argument variant`() {
        var received = emptyArray<String>()
        val command = HytaleCommand.TabooLibHytaleCommand(
            "root",
            "description",
            executor(),
            completer {
                received = it
                listOf("two")
            },
            structure(permission = "plugin.command.root", aliases = listOf("alias")),
        )
        val argument = command.requiredArguments.single()
        val variantsField = Class.forName("com.hypixel.hytale.server.core.command.system.AbstractCommand")
            .getDeclaredField("variantCommands")
        variantsField.isAccessible = true
        val variants = variantsField.get(command) as Map<*, *>

        assertEquals("plugin.command.root", command.permission)
        assertTrue(command.aliases.contains("alias"))
        assertFalse(argument.argumentType.isListArgument)
        assertEquals(listOf("two"), argument.getSuggestions(nativeSender(), arrayOf("tw")))
        assertArrayEquals(arrayOf("tw"), received)
        assertEquals("plugin.command.root", (variants[0] as AbstractCommand).permission)
    }

    @Test
    fun `command arguments keep positional input semantics`() {
        assertArrayEquals(emptyArray<String>(), commandArguments("root"))
        assertArrayEquals(arrayOf("one", "two"), commandArguments("root one   two"))
        assertArrayEquals(arrayOf("one"), commandArguments("  root one  "))
    }

    @Test
    fun `native command accepts zero and ordinary multi positional arguments`() {
        val executions = ArrayList<Array<String>>()
        val command = HytaleCommand.TabooLibHytaleCommand(
            "root",
            "description",
            executor { executions += it },
            completer(),
            structure(),
        )

        accept(command, "root")
        accept(command, "root one two")

        assertEquals(2, executions.size)
        assertArrayEquals(emptyArray<String>(), executions[0])
        assertArrayEquals(arrayOf("one", "two"), executions[1])
    }

    @Test
    fun `completion preserves current empty argument and invokes completer`() {
        assertArrayEquals(arrayOf(""), completionArguments(""))
        assertArrayEquals(arrayOf("one", ""), completionArguments("one "))
        assertArrayEquals(arrayOf("one", "two"), completionArguments("one   two"))

        var received = emptyArray<String>()
        val suggestions = commandSuggestions("one ") {
            received = it
            listOf("two")
        }

        assertArrayEquals(arrayOf("one", ""), received)
        assertEquals(listOf("two"), suggestions)
    }

    @Test
    fun `existing proxy senders keep identity`() {
        val adapter = HytaleAdapter()
        val player = proxy<ProxyPlayer>()
        val sender = proxy<ProxyCommandSender>()

        assertSame(player, adapter.adaptPlayer(player))
        assertSame(player, adapter.adaptCommandSender(player))
        assertSame(sender, adapter.adaptCommandSender(sender))
    }

    @Test
    fun `native command sender uses hytale wrapper`() {
        val adapter = HytaleAdapter()
        val sender = proxy<CommandSender>()

        val adapted = adapter.adaptCommandSender(sender)

        assertTrue(adapted is HytaleCommandSender)
        assertSame(sender, adapted.origin)
    }

    @Test
    fun `command dispatch never waits for incomplete future`() {
        val future = CompletableFuture<Void>()

        assertTrue(HytaleCommandSender.dispatchCommand { future })
        assertFalse(future.isDone)

        future.completeExceptionally(IllegalStateException("late failure"))
        assertTrue(future.isCompletedExceptionally)
    }

    @Test
    fun `command dispatch uses stable submission result`() {
        val failed = CompletableFuture<Void>().also {
            it.completeExceptionally(IllegalStateException("failed"))
        }
        val cancelled = CompletableFuture<Void>().also { it.cancel(false) }

        assertTrue(HytaleCommandSender.dispatchCommand { failed })
        assertTrue(HytaleCommandSender.dispatchCommand { cancelled })
    }

    @Test
    fun `async listener converts synchronous throw to failed future`() {
        val failure = IllegalStateException("boom")
        val result = invokeAsyncHandler(CompletableFuture<String>(), Function { throw failure })
        var observed: Throwable? = null
        result.whenComplete { _, ex -> observed = ex }

        assertTrue(result.isCompletedExceptionally)
        assertSame(failure, observed)
    }

    @Test
    fun `async listener rejects null future without blocking`() {
        @Suppress("UNCHECKED_CAST")
        val nullHandler = Proxy.newProxyInstance(
            HytaleCompatibilityTest::class.java.classLoader,
            arrayOf(Function::class.java),
        ) { _, method, _ -> if (method.name == "apply") null else defaultValue(method.returnType) }
            as Function<CompletableFuture<String>, CompletableFuture<String>>
        val result = invokeAsyncHandler(CompletableFuture<String>(), nullHandler)
        var observed: Throwable? = null
        result.whenComplete { _, ex -> observed = ex }

        assertTrue(result.isCompletedExceptionally)
        assertTrue(observed is NullPointerException)
    }

    @Test
    fun `quit callbacks run once and are removed`() {
        val session = Any()
        val first = AtomicInteger()
        val second = AtomicInteger()
        HytaleCommandSender.registerQuitCallback(session, Runnable(first::incrementAndGet))
        HytaleCommandSender.registerQuitCallback(session, Runnable(second::incrementAndGet))

        HytaleCommandSender.fireQuitCallbacks(session)
        HytaleCommandSender.fireQuitCallbacks(session)

        assertEquals(1, first.get())
        assertEquals(1, second.get())
    }

    private fun structure(permission: String = "", aliases: List<String> = emptyList()): CommandStructure {
        return CommandStructure(
            "root",
            aliases,
            "description",
            "",
            permission,
            "",
            PermissionDefault.TRUE,
            emptyMap(),
            false,
        )
    }

    private fun accept(command: HytaleCommand.TabooLibHytaleCommand, input: String) {
        val result = ParseResult()
        val tokens = requireNotNull(Tokenizer.parseArguments(input, result))
        val parser = ParserContext.of(tokens, result)
        val future = command.acceptCall(nativeSender(), parser, result)

        assertFalse(result.failed())
        future?.let {
            assertTrue(it.isDone)
            assertFalse(it.isCompletedExceptionally)
        }
    }

    private fun executor(block: (Array<String>) -> Unit = {}): CommandExecutor {
        return object : CommandExecutor {
            override fun execute(
                sender: ProxyCommandSender,
                command: CommandStructure,
                name: String,
                args: Array<String>,
            ): Boolean {
                block(args)
                return true
            }
        }
    }

    private fun completer(block: (Array<String>) -> List<String> = { emptyList() }): CommandCompleter {
        return object : CommandCompleter {
            override fun execute(
                sender: ProxyCommandSender,
                command: CommandStructure,
                name: String,
                args: Array<String>,
            ): List<String> {
                return block(args)
            }
        }
    }

    private fun nativeSender(): CommandSender {
        val uuid = UUID.randomUUID()
        return Proxy.newProxyInstance(CommandSender::class.java.classLoader, arrayOf(CommandSender::class.java)) { instance, method, args ->
            when (method.name) {
                "hasPermission" -> true
                "getDisplayName" -> "sender"
                "getUuid" -> uuid
                "equals" -> instance === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(instance)
                "toString" -> "CommandSenderProxy"
                else -> defaultValue(method.returnType)
            }
        } as CommandSender
    }

    private inline fun <reified T> proxy(): T {
        return Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { instance, method, args ->
            when (method.name) {
                "equals" -> instance === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(instance)
                "toString" -> "${T::class.java.simpleName}Proxy"
                else -> defaultValue(method.returnType)
            }
        } as T
    }

    private fun defaultValue(type: Class<*>): Any? {
        return when (type) {
            java.lang.Boolean.TYPE -> false
            java.lang.Byte.TYPE -> 0.toByte()
            java.lang.Short.TYPE -> 0.toShort()
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0F
            java.lang.Double.TYPE -> 0.0
            java.lang.Character.TYPE -> '\u0000'
            else -> null
        }
    }
}
