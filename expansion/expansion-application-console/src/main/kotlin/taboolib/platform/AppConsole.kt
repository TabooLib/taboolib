package taboolib.platform

import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder
import org.jline.reader.Candidate
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.pluginVersion

/**
 * @author Score2
 * @since 2022/06/08 13:37
 */
@Awake
@PlatformSide([Platform.APPLICATION])
object AppConsole : SimpleTerminalConsole(), ProxyCommandSender {

    val logger = LogManager.getLogger(AppConsole::class.java)

    override var isOp = true
    override val name = "CONSOLE"
    override val origin: Any = this

    @Awake(LifeCycle.LOAD)
    fun load() {
        System.setOut(IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream())
        System.setErr(IoBuilder.forLogger(logger).setLevel(Level.WARN).buildPrintStream())

        command("about", description = "about this server") {
            execute<ProxyCommandSender> { sender, context, argument ->
                sender.sendMessage("§r$pluginId v$pluginVersion")
                sender.sendMessage("§rThere are console application booting by §cTabooLib")
            }
        }
        command("help", aliases = listOf("?"), description = "suggest commands") {
            execute<ProxyCommandSender> { sender, context, argument ->
                sender.sendMessage("§rRegisted §a${AppCommand.commands.size} §rcommands, list:")
                AppCommand.commands.forEach {
                    sender.sendMessage("§r✦ §a${it.command.name}§r<${it.command.aliases.joinToString()}> §8- §2${it.command.description}")
                }
            }
        }
        command("stop", aliases = listOf("shutdown"), description = "stop the server") {
            execute<ProxyCommandSender> { sender, context, argument ->
                TabooLibCommon.testCancel()
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
        return !TabooLibCommon.isStopped()
    }

    override fun buildReader(builder: LineReaderBuilder): LineReader {
        builder
            .appName(pluginId)
            .completer { reader, line, candidates ->
                val buffer = line.line()
                AppCommand.suggest(buffer).forEach {
                    candidates.add(Candidate(it))
                }

            }
            .option(LineReader.Option.COMPLETE_IN_WORD, true);

        return super.buildReader(builder)
    }

    override fun runCommand(command: String) {
        AppCommand.runCommand(command)
    }

    override fun shutdown() {
        TabooLibCommon.testCancel()
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
        logger.info(message)
    }
}