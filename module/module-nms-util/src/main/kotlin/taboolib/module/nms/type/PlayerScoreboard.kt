package taboolib.module.nms.type

import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.module.nms.nmsScoreboard

class PlayerScoreboard(val player: Player) {

    private var currentTitle = ""
    private val currentContent = HashMap<Int, String>()
    private var prefix = ""
    private var suffix = ""
    private var color = ChatColorFormat.RESET

    var deleted = false
    var created = false

    init {
        nmsScoreboard.setupScoreboard(player, true)
        nmsScoreboard.display(player)
        sendTitle(currentTitle)
    }

    fun sendTitle(title: String) {
        if (currentTitle != title) {
            currentTitle = title
            nmsScoreboard.setDisplayName(player, title)
        }
    }

    fun sendContent(lines: List<String>) {
        if (deleted) {
            nmsScoreboard.setupScoreboard(player, false, currentTitle)
            nmsScoreboard.display(player)
        }
        deleted = nmsScoreboard.changeContent(player, lines, currentContent)
        currentContent.clear()
        currentContent.putAll(lines.mapIndexed { index, s -> index to s }.toMap())
    }

    fun setPrefix(prefix: String, target: Player?) {
        this.prefix = prefix
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }

    fun clearPrefix(target: Player?) {
        this.prefix = ""
        this.created = true
        nmsScoreboard.updateTeam(player, "", suffix, color, !created, target)
    }

    fun setSuffix(suffix: String, target: Player?) {
        this.suffix = suffix
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }

    fun clearSuffix(target: Player?) {
        this.suffix = ""
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, "", color, !created, target)
    }
    
    fun setColor(color: ChatColorFormat, target: Player?) {
        this.color = color
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }
}