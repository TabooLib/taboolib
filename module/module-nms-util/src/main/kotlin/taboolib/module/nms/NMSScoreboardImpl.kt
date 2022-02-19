package taboolib.module.nms

import net.minecraft.server.v1_16_R3.*
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.common.reflect.Reflex.Companion.unsafeInstance
import java.util.*

class NMSScoreboardImpl : NMSScoreboard() {

    override fun setupScoreboard(player: Player, color: Boolean, title: String) {
        val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", "TabooScore")
            packet.setProperty("displayName", ChatComponentText(title))
            packet.setProperty("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("method", 0)
        } else {
            packet.setProperty("a", "TabooScore")
            if (MinecraftVersion.major >= 5) {
                packet.setProperty("b", ChatComponentText(title))
            } else {
                packet.setProperty("b", title)
            }
            packet.setProperty("c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("d", 0)
        }
        player.sendPacket(packet)
        if (color) {
            initTeam(player)
        }
    }

    override fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>): Boolean {
        if (content.isEmpty()) {
            val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
            if (MinecraftVersion.isUniversal) {
                packet.setProperty("objectiveName", "TabooScore")
                packet.setProperty("displayName", ChatComponentText("ScoreBoard"))
                packet.setProperty("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
                packet.setProperty("method", 1)
            } else {
                packet.setProperty("a", "TabooScore")
                if (MinecraftVersion.major >= 5) {
                    packet.setProperty("b", ChatComponentText("ScoreBoard"))
                } else {
                    packet.setProperty("b", "ScoreBoard")
                }
                packet.setProperty("c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
                packet.setProperty("d", 1)
            }
            player.sendPacket(packet)
            return true
        }
        val update = content.size != lastContent.size
        if (update) {
            updateLineCount(player, content.size, lastContent.size)
        }
        content.forEachIndexed { line, ct ->
            if (update || ct != lastContent[line]) {
                sendTeamPrefixSuffix(player, uniqueColors[content.size - line - 1], ct)
            }
        }
        return false
    }

    override fun display(player: Player) {
        val packet = PacketPlayOutScoreboardDisplayObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("slot", 1)
            packet.setProperty("objectiveName", "TabooScore")
        } else {
            packet.setProperty("a", 1)
            packet.setProperty("b", "TabooScore")
        }
        player.sendPacket(packet)
    }

    override fun setDisplayName(player: Player, title: String) {
        val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", "TabooScore")
            packet.setProperty("displayName", ChatComponentText(title))
            packet.setProperty("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("method", 2)
        } else {
            packet.setProperty("a", "TabooScore")
            if (MinecraftVersion.major >= 5) {
                packet.setProperty("b", ChatComponentText(title))
            } else {
                packet.setProperty("b", title)
            }
            packet.setProperty("c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("d", 2)
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
        uniqueColors.forEach { color ->
            if (MinecraftVersion.isUniversal) {
                val packet = PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
                packet.setProperty("method", 0)
                packet.setProperty("name", color)
                packet.setProperty("players", listOf(color))
                val b = universalTeamData.unsafeInstance()
                b.setProperty("displayName", ChatComponentText(color))
                b.setProperty("nametagVisibility", "always")
                b.setProperty("collisionRule", "always")
                b.setProperty("color", EnumChatFormat.RESET)
                b.setProperty("options", 3)
                packet.setProperty("parameters", Optional.of(b))
                player.sendPacket(packet)
                return@forEach
            }
            if (MinecraftVersion.major >= 5) {
                val packet = PacketPlayOutScoreboardTeam()
                packet.setProperty("a", color)
                packet.setProperty("b", ChatComponentText(color))
                packet.setProperty("e", "always")
                packet.setProperty("f", "always")
                packet.setProperty("g", EnumChatFormat.RESET)
                packet.setProperty("h", listOf(color))
                packet.setProperty("i", 0)
                packet.setProperty("j", -1)
                player.sendPacket(packet)
                return@forEach
            }
            val packet = PacketPlayOutScoreboardTeam()
            packet.setProperty("a", color)
            packet.setProperty("b", color)
            packet.setProperty("e", ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e)
            // Collections$SingletonList cannot be cast to java.lang.Number
            if (MinecraftVersion.major >= 1) {
                packet.setProperty("f", "always")
                packet.setProperty("g", -1)
                packet.setProperty("h", listOf(color))
                packet.setProperty("i", 0)
            } else {
                packet.setProperty("f", -1)
                packet.setProperty("g", listOf(color))
                packet.setProperty("h", 0)
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
            packet.setProperty("method", 2)
            packet.setProperty("name", team)
            packet.setProperty("players", listOf(team))
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", ChatComponentText(team))
            b.setProperty("playerPrefix", ChatComponentText(content))
            b.setProperty("nametagVisibility", "always")
            b.setProperty("collisionRule", "always")
            b.setProperty("color", EnumChatFormat.RESET)
            b.setProperty("options", 3)
            packet.setProperty("parameters", Optional.of(b))
            player.sendPacket(packet)
            return
        }
        if (MinecraftVersion.major >= 5) {
            val packet = PacketPlayOutScoreboardTeam()
            packet.setProperty("a", team) // 1.17 -> name
            packet.setProperty("c", ChatComponentText(content)) // 1.17 -> playerPrefix
            packet.setProperty("i", 2) // 1.17 -> method
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
        packet.setProperty("a", team)
        packet.setProperty(if (MinecraftVersion.major >= 1) "i" else "h", 2)
        packet.setProperty("c", prefix)
        packet.setProperty("d", suffix)
        player.sendPacket(packet)
    }

    private fun updateLineCount(player: Player, line: Int, lastLineCount: Int) {
        validateLineCount(line)
        if (line > lastLineCount) {
            (lastLineCount until line).forEach { i ->
                if (MinecraftVersion.major >= 9) {
                    val packet = PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.setProperty("owner", uniqueColors[i])
                    packet.setProperty("objectiveName", "TabooScore")
                    packet.setProperty("score", i)
                    packet.setProperty("method", ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.major >= 5) {
                    val packet = PacketPlayOutScoreboardScore()
                    packet.setProperty("a", uniqueColors[i])
                    packet.setProperty("b", "TabooScore")
                    packet.setProperty("c", i)
                    packet.setProperty("d", ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueColors[i])
                packet.setProperty("b", "TabooScore")
                packet.setProperty("c", i)
                packet.setProperty("d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE)
                player.sendPacket(packet)
            }
        } else {
            (line until lastLineCount).forEach { i ->
                if (MinecraftVersion.major >= 9) {
                    val packet = PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.setProperty("owner", uniqueColors[i])
                    packet.setProperty("objectiveName", "TabooScore")
                    packet.setProperty("method", ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.major >= 5) {
                    val packet = PacketPlayOutScoreboardScore()
                    packet.setProperty("a", uniqueColors[i])
                    packet.setProperty("b", "TabooScore")
                    packet.setProperty("d", ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueColors[i])
                packet.setProperty("b", "TabooScore")
                packet.setProperty("d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.REMOVE)
                player.sendPacket(packet)
            }
        }
    }
}