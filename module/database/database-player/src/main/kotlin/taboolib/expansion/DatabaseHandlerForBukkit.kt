package taboolib.expansion

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer

/**
 * 获取玩家的数据容器。
 *
 * @return 玩家的数据容器
 */
fun Player.getDataContainer(): DataContainer {
    return adaptPlayer(this).getDataContainer()
}

/**
 * 为玩家设置数据容器。
 *
 * @param usernameMode 是否使用用户名模式，默认为 false
 */
fun Player.setupDataContainer(usernameMode: Boolean = false) {
    adaptPlayer(this).setupDataContainer(usernameMode)
}

/**
 * 释放玩家的数据容器。
 */
fun Player.releaseDataContainer() {
    adaptPlayer(this).releaseDataContainer()
}

/**
 * 获取玩家的自动数据容器。
 *
 * @return 玩家的自动数据容器
 */
fun Player.getAutoDataContainer(): AutoDataContainer {
    return this.uniqueId.getAutoDataContainer()
}

/**
 * 释放玩家的自动数据容器。
 */
fun Player.releaseAutoDataContainer() {
    this.uniqueId.releaseAutoDataContainer()
}