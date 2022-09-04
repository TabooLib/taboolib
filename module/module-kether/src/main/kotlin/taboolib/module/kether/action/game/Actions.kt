package taboolib.module.kether.action.game

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.isConsole
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import taboolib.module.kether.*

object Actions {

    @KetherParser(["tell", "send", "message"])
    fun actionTell() = scriptParser {
        val message = it.nextParsedAction()
        actionTake { run(message).str { s -> script().sender?.sendMessage(s.replace("@sender", script().sender?.name ?: "null")) } }
    }

    @KetherParser(["actionbar"])
    fun actionActionBar() = scriptParser {
        val message = it.nextParsedAction()
        actionTake { run(message).str { o -> player().sendActionBar(o.replace("@sender", player().name)) } }
    }

    @KetherParser(["broadcast", "bc"])
    fun actionBroadcast() = scriptParser {
        val message = it.nextParsedAction()
        actionTake { run(message).str { o -> onlinePlayers().forEach { p -> p.sendMessage(o.replace("@sender", player().name)) } } }
    }

    @KetherParser(["color", "colored"])
    fun actionColor() = scriptParser {
        val message = it.nextParsedAction()
        actionTake { run(message).str { o -> o.colored() } }
    }

    @Suppress("SpellCheckingInspection")
    @KetherParser(["uncolor", "uncolored"])
    fun actionUncolored() = scriptParser {
        val message = it.nextParsedAction()
        actionTake { run(message).str { o -> o.uncolored() } }
    }

    @KetherParser(["perm", "permission"])
    fun actionPermission() = scriptParser {
        val perm = it.nextParsedAction()
        actionTake { run(perm).str { s -> player().hasPermission(s) } }
    }

    @KetherParser(["players"])
    fun actionPlayers() = scriptParser {
        actionNow { onlinePlayers().map { it.name } }
    }

    @KetherParser(["sender"])
    fun actionSender() = scriptParser {
        actionNow { if (script().sender.isConsole()) "console" else script().sender?.name ?: "null" }
    }

    @KetherParser(["switch"])
    fun actionSwitch() = scriptParser {
        val sender = it.nextParsedAction()
        actionTake {
            run(sender).str { s ->
                if (s == "console" || s == "server") {
                    script().sender = console()
                } else {
                    script().sender = getProxyPlayer(s)
                }
            }
        }
    }
}