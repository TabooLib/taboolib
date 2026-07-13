package taboolib.platform

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.LifeCycle
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.service.PlatformExecutor
import java.lang.reflect.Modifier
import java.util.concurrent.CompletableFuture
import java.util.concurrent.FutureTask
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicInteger

class ApplicationPlatformTest {

    @AfterEach
    fun cleanupCommands() {
        AppCommand.commands.clear()
    }

    @Test
    fun `initialization lifecycle reaches active in order`() {
        assertEquals(
            listOf(LifeCycle.CONST, LifeCycle.INIT, LifeCycle.LOAD, LifeCycle.ENABLE, LifeCycle.ACTIVE),
            AppLifeCycle.initialization()
        )
    }

    @Test
    fun `shutdown during enable prevents active lifecycle regression`() {
        val lifeCycle = AppLifeCycle()
        val calls = ArrayList<LifeCycle>()

        val running = lifeCycle.run {
            calls += it
            if (it == LifeCycle.ENABLE) {
                lifeCycle.shutdown { calls += it }
            }
        }
        lifeCycle.shutdown { calls += it }

        assertFalse(running)
        assertEquals(
            listOf(LifeCycle.CONST, LifeCycle.INIT, LifeCycle.LOAD, LifeCycle.ENABLE, LifeCycle.DISABLE),
            calls
        )
    }

    @Test
    fun `console stops running at disable`() {
        assertTrue(isApplicationRunning(true, false))
        assertFalse(isApplicationRunning(false, false))
        assertFalse(isApplicationRunning(true, true))
        assertTrue(Modifier.isVolatile(App::class.java.getDeclaredField("running").modifiers))
    }

    @Test
    fun `command unregister matches primary name and aliases`() {
        val service = AppCommand()
        val primary = command("primary", listOf("alias"))
        val other = command("other", listOf("secondary"))
        primary.register()
        other.register()

        service.unregisterCommand("PRIMARY")
        assertEquals(setOf(other), AppCommand.commands)

        service.unregisterCommand("SECONDARY")
        assertTrue(AppCommand.commands.isEmpty())
    }

    @Test
    fun `command bulk unregister clears concurrent set`() {
        val service = AppCommand()
        command("one").register()
        command("two").register()

        service.unregisterCommands()

        assertTrue(AppCommand.commands.isEmpty())
        assertEquals(java.util.Set::class.java, AppCommand.Companion::class.java.getMethod("getCommands").returnType)
    }

    @Test
    fun `executor has explicit lifecycle and rejects all tasks after stop`() {
        val executor = AppExecutor()
        try {
            assertEquals(AppExecutor.State.NEW, executor.currentState())
            executor.start()
            assertEquals(AppExecutor.State.RUNNING, executor.currentState())
            executor.stop()
            assertEquals(AppExecutor.State.STOPPED, executor.currentState())

            assertThrows(RejectedExecutionException::class.java) {
                executor.submit(runnable(now = true) {})
            }
            assertThrows(RejectedExecutionException::class.java) {
                executor.submit(runnable(now = false) {})
            }
        } finally {
            executor.stop()
        }
    }

    @Test
    fun `executor keeps immediate pre-start behavior and exposes task failures`() {
        val executor = AppExecutor()
        val executions = AtomicInteger()
        try {
            executor.submit(runnable(now = true) { executions.incrementAndGet() })
            assertEquals(1, executions.get())
            assertThrows(IllegalStateException::class.java) {
                executor.submit(runnable(now = true) { error("observable") })
            }
        } finally {
            executor.stop()
        }
    }

    @Test
    fun `executor task failure is reported and rethrown unchanged`() {
        val failure = IllegalStateException("boom")
        var reported: Throwable? = null

        val thrown = assertThrows(IllegalStateException::class.java) {
            runAppTask({ reported = it }) { throw failure }
        }

        assertTrue(reported === failure)
        assertTrue(thrown === failure)
    }

    @Test
    fun `executor task cancellation is idempotent and worker threads are named`() {
        val task = AppExecutor.AppPlatformTask()
        val future = RecordingFuture()
        task.cancel()
        task.attach(future)
        task.cancel()
        assertTrue(task.isCancelled)
        assertEquals(1, future.cancelCount)

        val cancellationSignal = CompletableFuture<Unit>()
        val compatibleTask = AppExecutor.AppPlatformTask(cancellationSignal)
        compatibleTask.cancel()
        compatibleTask.cancel()
        assertTrue(cancellationSignal.isDone)
        AppExecutor.AppPlatformTask::class.java.getConstructor(CompletableFuture::class.java)

        val factory = AppExecutorThreadFactory()
        assertEquals("TabooLib-Application-Executor-1", factory.newThread {}.name)
        assertEquals("TabooLib-Application-Executor-2", factory.newThread {}.name)
    }

    private fun runnable(now: Boolean, block: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformRunnable {
        return PlatformExecutor.PlatformRunnable(now, async = false, delay = 0, period = 0, executor = block)
    }

    private class RecordingFuture : FutureTask<Unit>(Runnable {}, Unit) {

        var cancelCount = 0

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            cancelCount++
            return super.cancel(mayInterruptIfRunning)
        }
    }

    private fun command(name: String, aliases: List<String> = emptyList()): AppCommand.Command {
        val structure = CommandStructure(
            name,
            aliases,
            "",
            "",
            "",
            "",
            PermissionDefault.TRUE,
            emptyMap(),
            false
        )
        val executor = object : CommandExecutor {
            override fun execute(
                sender: taboolib.common.platform.ProxyCommandSender,
                command: CommandStructure,
                name: String,
                args: Array<String>
            ): Boolean = true
        }
        val completer = object : CommandCompleter {
            override fun execute(
                sender: taboolib.common.platform.ProxyCommandSender,
                command: CommandStructure,
                name: String,
                args: Array<String>
            ): List<String> = emptyList()
        }
        return AppCommand.Command(structure, executor, completer) {}
    }
}
