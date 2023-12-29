package taboolib.module.kether.action.game

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.platformLocation
import taboolib.common.util.Location
import taboolib.common.util.isConsole
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import taboolib.module.kether.*

internal object Actions {

    @KetherParser(["tell", "send", "message"])
    fun actionTell() = combinationParser {
        it.group(text()).apply(it) { str ->
            now { script().sender?.sendMessage(str.replace("@sender", script().sender?.name.toString())) ?: error("No sender") }
        }
    }

    @KetherParser(["actionbar"])
    fun actionActionBar() = combinationParser {
        it.group(text()).apply(it) { str ->
            now { player().sendActionBar(str.replace("@sender", script().sender?.name.toString())) }
        }
    }

    @KetherParser(["broadcast", "bc"])
    fun actionBroadcast() = combinationParser {
        it.group(text()).apply(it) { str ->
            now { onlinePlayers().forEach { p -> p.sendMessage(str.replace("@sender", script().sender?.name.toString())) } }
        }
    }

    @KetherParser(["color", "colored"])
    fun actionColor() = combinationParser {
        it.group(text()).apply(it) { str -> now { str.colored() } }
    }

    @Suppress("SpellCheckingInspection")
    @KetherParser(["uncolor", "uncolored"])
    fun actionUncolored() = combinationParser {
        it.group(text()).apply(it) { str -> now { str.uncolored() } }
    }

    @KetherParser(["perm", "permission"])
    fun actionPermission() = combinationParser {
        it.group(text()).apply(it) { perm -> now { player().hasPermission(perm) } }
    }

    @KetherParser(["players"])
    fun actionPlayers() = scriptParser {
        actionNow { onlinePlayers().map { it.name } }
    }

    @KetherParser(["sender"])
    fun actionSender() = scriptParser {
        actionNow { if (script().sender.isConsole()) "console" else script().sender?.name.toString() }
    }

    @KetherParser(["switch"])
    fun actionSwitch() = combinationParser {
        it.group(text()).apply(it) { to ->
            now { script().sender = if (to == "console" || to == "server") console() else getProxyPlayer(to) }
        }
    }

    @KetherParser(["title"])
    fun actionTitle() = combinationParser {
        it.group(
            text(),
            command("subtitle", then = text()).option(),
            command("by", "with", then = int().and(int(), int())).option().defaultsTo(Triple(0, 20, 0))
        ).apply(it) { t1, t2, time ->
            val (i, s, o) = time
            now { player().sendTitle(t1.replace("@sender", player().name), t2?.replace("@sender", player().name) ?: "§r", i, s, o) }
        }
    }

    @KetherParser(["subtitle"])
    fun actionSubtitle() = combinationParser {
        it.group(
            text(),
            command("by", "with", then = int().and(int(), int())).option().defaultsTo(Triple(0, 20, 0))
        ).apply(it) { text, time ->
            val (i, s, o) = time
            now { player().sendTitle("§r", text.replace("@sender", player().name), i, s, o) }
        }
    }

    @KetherParser(["loc", "location"])
    fun actionLocation() = combinationParser {
        it.group(
            text(),
            double(),
            double(),
            double(),
            command("and", then = float().and(float())).option().defaultsTo(0f to 0f)
        ).apply(it) { world, x, y, z, (yaw, pitch) ->
            now { platformLocation<Any>(Location(world, x, y, z, yaw, pitch)) }
        }
    }

    @PlatformSide([Platform.BUKKIT])
    @KetherParser(["sound"])
    fun actionSound() = combinationParser {
        it.group(
            text(),
            command("by", "with", then = float().and(float())).option().defaultsTo(0f to 0f)
        ).apply(it) { sound, vp ->
            val (v, p) = vp
            now {
                if (sound.startsWith("resource:")) {
                    player().playSoundResource(player().location, sound.substringAfter("resource:"), v, p)
                } else {
                    player().playSound(player().location, sound.replace('.', '_').uppercase(), v, p)
                }
            }
        }
    }

    @PlatformSide([Platform.BUKKIT])
    @KetherParser(["stopsound"])
    fun actionStopSound() = combinationParser {
        it.group(
            text(),
        ).apply(it) { sound ->
            now {
                if (sound.startsWith("resource:")) {
                    player().playSoundResource(player().location, sound.substringAfter("resource:"), -1f, -1f)
                } else {
                    player().playSound(player().location, sound.replace('.', '_').uppercase(), -1f, -1f)
                }
            }
        }
    }
}