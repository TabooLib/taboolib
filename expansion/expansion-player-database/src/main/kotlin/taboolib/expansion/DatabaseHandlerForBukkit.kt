package taboolib.expansion

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer

fun Player.getDataContainer(): DataContainer {
    return adaptPlayer(this).getDataContainer()
}

fun Player.setupDataContainer(usernameMode: Boolean = false) {
    adaptPlayer(this).setupDataContainer(usernameMode)
}

fun Player.releaseDataContainer() {
    adaptPlayer(this).releaseDataContainer()
}

fun Player.getAutoDataContainer(): AutoDataContainer {
    return this.uniqueId.getAutoDataContainer()
}

fun Player.releaseAutoDataContainer() {
    this.uniqueId.releaseAutoDataContainer()
}
