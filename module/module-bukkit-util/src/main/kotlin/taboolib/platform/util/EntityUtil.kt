package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.type.BukkitEquipment

/**
 * 获取生物脚下的方块.
 */
val Entity.groundBlock: Block
    get() = location.add(0.0, -0.01, 0.0).block

/**
 * 获取生物脚下方块的材质.
 */
val Entity.groundBlockType: Material
    get() = groundBlock.type

/**
 * 获取装备
 */
fun LivingEntity.getEquipment(slot: BukkitEquipment): ItemStack? {
    return slot.getItem(this)
}

/**
 * 修改装备
 */
fun LivingEntity.setEquipment(slot: BukkitEquipment, item: ItemStack) {
    slot.setItem(this, item)
}

/**
 * 转换为安全实体类
 */
fun <T : Entity> T.safely(): SafeEntity<T> {
    return SafeEntity(this)
}

/**
 * 安全实体类
 */
@Suppress("UNCHECKED_CAST")
class SafeEntity<T : Entity>(private var entity: T) {

    /**
     * 在特定情况下，玩家实体可能会失效，因此需要重新从服务器获取
     */
    fun get(): T {
        if (entity is Player && !entity.isValid) {
            val playerExact = Bukkit.getPlayerExact(entity.name)
            if (playerExact != null) {
                entity = playerExact as T
            } else {
                error("Player ${entity.name} is offline.")
            }
        }
        return entity
    }

    /**
     * 获取实体，如果实体失效则返回 null
     */
    fun getOrNull(): T? {
        return runCatching { get() }.getOrNull()
    }

    /**
     * 是否有效
     */
    fun isValid(): Boolean {
        return getOrNull() != null
    }
}