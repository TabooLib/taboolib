package taboolib.module.nms

import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import taboolib.module.nms.type.LightType


/**
 * 创建光源
 *
 * @param lightLevel 光照等级
 * @param lightType 光源类型
 * @param update 是否更新区块光照
 * @param viewers 可见玩家
 */
fun Block.createLight(
    lightLevel: Int,
    lightType: LightType = LightType.ALL,
    update: Boolean = true,
    viewers: Collection<Player> = Bukkit.getOnlinePlayers(),
): Boolean {
    if (MinecraftVersion.majorLegacy < 11200) {
        error("Not supported yet.")
    }
    if (nmsGeneric.getRawLightLevel(this, lightType) > lightLevel) {
        nmsGeneric.deleteLight(this, lightType)
    }
    val result = nmsGeneric.createLight(this, lightType, lightLevel)
    if (update) {
        updateLight(lightType, viewers)
    }
    return result
}

/**
 * 删除光源
 *
 * @param lightType 光源类型
 * @param update 是否更新区块光照
 * @param viewers 可见玩家
 */
fun Block.deleteLight(
    lightType: LightType = LightType.ALL,
    update: Boolean = true,
    viewers: Collection<Player> = Bukkit.getOnlinePlayers(),
): Boolean {
    if (MinecraftVersion.majorLegacy < 11200) {
        error("Not supported yet.")
    }
    val result = nmsGeneric.deleteLight(this, lightType)
    if (update) {
        updateLight(lightType, viewers)
    }
    return result
}

/**
 * 更新光照
 */
fun Block.updateLight(lightType: LightType, viewers: Collection<Player>) {
    if (MinecraftVersion.isUniversal) {
        nmsGeneric.updateLightUniversal(this, lightType, viewers)
    } else {
        // 更新邻边区块 (为了防止光只在一个区块的尴尬局面)
        (-1..1).forEach { x ->
            (-1..1).forEach { z ->
                nmsGeneric.updateLight(world.getChunkAt(chunk.x + x, chunk.z + z), viewers)
            }
        }
    }
}