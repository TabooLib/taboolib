package taboolib.module.nms

import net.minecraft.server.v1_16_R3.*
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.unsafeInstance
import java.util.*

class NMSScoreboardImpl : NMSScoreboard() {

    override fun setupScoreboard(player: Player, remove: Boolean) {
        val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.reflex("objectiveName", if (remove) "REMOVE" else "TabooScore")
            packet.reflex("displayName", ChatComponentText("ScoreBoard"))
            packet.reflex("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.reflex("method", 0)
        } else {
            packet.reflex("a", if (remove) "REMOVE" else "TabooScore")
            if (MinecraftVersion.major >= 5) {
                packet.reflex("b", ChatComponentText("ScoreBoard"))
            } else {
                packet.reflex("b", "ScoreBoard")
            }
            packet.reflex("c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.reflex("d", 0)
        }
        player.sendPacket(packet)
        initTeam(player)
    }

    override fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>) {
        if (content.size != lastContent.size) {
            updateLineCount(player, content.size, lastContent.size)
        }
        content.forEachIndexed { line, ct ->
            if (ct != lastContent[content.size - line - 1]) {
                sendTeamPrefixSuffix(player, uniqueColors[content.size - line - 1], ct)
            }
        }
    }

    override fun display(player: Player) {
        val packet = PacketPlayOutScoreboardDisplayObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.reflex("slot", 1)
            packet.reflex("objectiveName", "TabooScore")
        } else {
            packet.reflex("a", 1)
            packet.reflex("b", "TabooScore")
        }
        player.sendPacket(packet)
    }

    override fun setDisplayName(player: Player, title: String) {
        val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.reflex("objectiveName", "TabooScore")
            packet.reflex("displayName", ChatComponentText(title))
            packet.reflex("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.reflex("method", 2)
        } else {
            packet.reflex("a", "TabooScore")
            if (MinecraftVersion.major >= 5) {
                packet.reflex("b", ChatComponentText(title))
            } else {
                packet.reflex("b", title)
            }
            packet.reflex("c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.reflex("d", 2)
        }
        player.sendPacket(packet)
    }

    /**
     *
     * a -> Team Name
     * b -> Team Display Name
     * c -> Team Prefix
     * d -> Team Suffix
     * e -> Name Tag Visibility
     * f -> Color
     * g -> Players, Player Count
     * h -> Mode
     *
     *  If 0 then the team is created.
     *  If 1 then the team is removed.
     *  If 2 the team team information is updated.
     *  If 3 then new players are added to the team.
     *  If 4 then players are removed from the team.
     *
     * i -> Friendly Fire
     *
     * @see EnumChatFormat
     * @see PacketPlayOutScoreboardTeam
     */
    private fun initTeam(player: Player) {
        uniqueColors.forEachIndexed { _, color ->
            if (MinecraftVersion.major >= 9) {
                val packet = PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
                packet.reflex("method", 0)
                packet.reflex("name", color)
                packet.reflex("players", listOf(color))
                val b = universalTeamData.unsafeInstance()
                b.reflex("displayName", ChatComponentText(color))
                b.reflex("nametagVisibility", "always")
                b.reflex("collisionRule", "always")
                b.reflex("color", EnumChatFormat.RESET)
                b.reflex("options", -1)
                packet.reflex("parameters", Optional.of(b))
                return@forEachIndexed
            }
            if (MinecraftVersion.major >= 5) {
                val packet = PacketPlayOutScoreboardTeam()
                packet.reflex("a", color)
                packet.reflex("b", ChatComponentText(color))
                packet.reflex("e", "always")
                packet.reflex("f", "always")
                packet.reflex("g", EnumChatFormat.RESET)
                packet.reflex("h", listOf(color))
                packet.reflex("i", 0)
                packet.reflex("j", -1)
                player.sendPacket(packet)
                return@forEachIndexed
            }
            val packet = PacketPlayOutScoreboardTeam()
            packet.reflex("a", color)
            packet.reflex("b", color)
            packet.reflex("e", ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e)
            // Collections$SingletonList cannot be cast to java.lang.Number
            if (MinecraftVersion.major >= 1) {
                packet.reflex("f", "always")
                packet.reflex("g", -1)
                packet.reflex("h", listOf(color))
                packet.reflex("i", 0)
            } else {
                packet.reflex("f", -1)
                packet.reflex("g", listOf(color))
                packet.reflex("h", 0)
            }
            player.sendPacket(packet)
        }
    }

    private fun validateLineCount(line: Int) {
        if (uniqueColors.size < line) {
            throw IllegalArgumentException("Lines size are larger than supported.")
        }
    }

    /**
     * @param team 为\[content.size - line - 1\]
     */
    private fun sendTeamPrefixSuffix(player: Player, team: String, content: String) {
        if (MinecraftVersion.major >= 9) {
            val packet = PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.reflex("method", 2)
            packet.reflex("name", team)
            packet.reflex("players", listOf(team))
            val b = universalTeamData.unsafeInstance()
            b.reflex("displayName", ChatComponentText(team))
            b.reflex("playerPrefix", ChatComponentText(content))
            b.reflex("nametagVisibility", "always")
            b.reflex("collisionRule", "always")
            b.reflex("color", EnumChatFormat.RESET)
            b.reflex("options", -1)
            packet.reflex("parameters", Optional.of(b))
            player.sendPacket(packet)
            return
        }
        if (MinecraftVersion.major >= 5) {
            val packet = PacketPlayOutScoreboardTeam()
            packet.reflex("a", team) // 1.17 -> name
            packet.reflex("c", ChatComponentText(content)) // 1.17 -> playerPrefix
            packet.reflex("i", 2) // 1.17 -> method
            player.sendPacket(packet)
            return
        }
        var prefix = content
        var suffix = ""
        if (content.length > 16) {
            prefix = content.substring(0 until 16)
            val color = ChatColor.getLastColors(prefix)
            suffix = color + content.substring(16 until content.length)
            if (suffix.length > 16) {
                suffix = suffix.substring(0, 16)
            }
        }
        val packet = PacketPlayOutScoreboardTeam()
        packet.reflex("a", team)
        packet.reflex(if (MinecraftVersion.major >= 1) "i" else "h", 2)
        packet.reflex("c", prefix)
        packet.reflex("d", suffix)
        player.sendPacket(packet)
    }

    private fun updateLineCount(player: Player, line: Int, lastLineCount: Int) {
        validateLineCount(line)
        if (line > lastLineCount) {
            (lastLineCount until line).forEach { i ->
                if (MinecraftVersion.major >= 9) {
                    val packet = PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.reflex("owner", uniqueColors[i])
                    packet.reflex("objectiveName", "TabooScore")
                    packet.reflex("score", i)
                    packet.reflex("method", ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.major >= 5) {
                    val packet = PacketPlayOutScoreboardScore()
                    packet.reflex("a", uniqueColors[i])
                    packet.reflex("b", "TabooScore")
                    packet.reflex("c", i)
                    packet.reflex("d", ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = PacketPlayOutScoreboardScore()
                packet.reflex("a", uniqueColors[i])
                packet.reflex("b", "TabooScore")
                packet.reflex("c", i)
                packet.reflex("d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE)
                player.sendPacket(packet)
            }
        } else {
            (line until lastLineCount).forEach { i ->
                if (MinecraftVersion.major >= 9) {
                    val packet = PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.reflex("owner", uniqueColors[i])
                    packet.reflex("objectiveName", "TabooScore")
                    packet.reflex("method", ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.major >= 5) {
                    val packet = PacketPlayOutScoreboardScore()
                    packet.reflex("a", uniqueColors[i])
                    packet.reflex("b", "TabooScore")
                    packet.reflex("d", ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = PacketPlayOutScoreboardScore()
                packet.reflex("a", uniqueColors[i])
                packet.reflex("b", "TabooScore")
                packet.reflex("d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.REMOVE)
                player.sendPacket(packet)
            }
        }
    }
}