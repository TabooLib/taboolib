package taboolib.platform

import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.jline.reader.Candidate
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.pluginVersion

/**
 * @author Score2
 * @since 2022/06/08 13:37
 */
@Awake
@Inject
@PlatformSide(Platform.APPLICATION)
object AppConsole : SimpleTerminalConsole(), ProxyCommandSender {

    // val logger: Logger = LogManager.getLogger(AppConsole::class.java)

    override var isOp = true
    override val name = "CONSOLE"
    override val origin: Any = this

    @Awake(LifeCycle.LOAD)
    fun load() {
        // System.setOut(IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream())
        // System.setErr(IoBuilder.forLogger(logger).setLevel(Level.WARN).buildPrintStream())
        // 注册命令
        command("about", description = "about this server") {
            execute<ProxyCommandSender> { sender, _, _ ->
                sender.sendMessage("$pluginId v$pluginVersion")
                sender.sendMessage("There are console application booting by TabooLib")
            }
        }
        command("help", aliases = listOf("?"), description = "suggest commands") {
            execute<ProxyCommandSender> { sender, _, _ ->
                sender.sendMessage("Registed ${AppCommand.commands.size} commands, list:")
                AppCommand.commands.forEach {
                    sender.sendMessage("✦ ${it.command.name}<${it.command.aliases.joinToString()}> - ${it.command.description}")
                }
            }
        }
        command("stop", aliases = listOf("shutdown"), description = "stop the server") {
            execute<ProxyCommandSender> { _, _, _ ->
                App.shutdown()
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun enable() {
        object : Thread("console handler") {
            override fun run() {
                AppConsole.start()
            }
        }.run {
            isDaemon = true
            start()
        }
    }

    override fun isRunning(): Boolean {
        return !TabooLib.isStopped()
    }

    override fun buildReader(builder: LineReaderBuilder): LineReader {
        builder
            .appName(pluginId)
            .completer { _, line, candidates ->
                val buffer = line.line()
                AppCommand.suggest(buffer).forEach { candidates.add(Candidate(it)) }
            }.option(LineReader.Option.COMPLETE_IN_WORD, true);
        return super.buildReader(builder)
    }

    override fun runCommand(command: String) {
        AppCommand.runCommand(command)
    }

    override fun shutdown() {
        App.shutdown()
    }

    override fun hasPermission(permission: String): Boolean {
        return true
    }

    override fun isOnline(): Boolean {
        return true
    }

    override fun performCommand(command: String): Boolean {
        AppCommand.runCommand(command)
        return true
    }

    override fun sendMessage(message: String) {
        info(message)
    }
}