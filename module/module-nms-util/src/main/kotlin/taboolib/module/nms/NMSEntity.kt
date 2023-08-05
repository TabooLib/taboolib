package taboolib.module.nms

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.entity.CreatureSpawnEvent
import taboolib.common.util.unsafeLazy
import java.util.function.Consumer

/**
 * TabooLib
 * taboolib.module.nms.NMSEntity
 *
 * @author 坏黑
 * @since 2023/8/5 03:47
 */
abstract class NMSEntity {

    /**
     * 在世界中生成实体
     */
    abstract fun <T : Entity> spawnEntity(location: Location, entity: Class<T>, callback: Consumer<T>): T

    /**
     * 获取实体类型
     */
    abstract fun getEntityType(name: String): Any
}

/**
 * [NMSEntity] 的实现类
 */
class NMSEntityImpl : NMSEntity() {
    
    override fun getEntityType(name: String): Any {
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_14)) {
            net.minecraft.server.v1_14_R1.EntityTypes.a(name).orElse(null)
        } else {
            net.minecraft.server.v1_13_R2.EntityTypes.a(name)!!
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> spawnEntity(location: Location, entity: Class<T>, callback: Consumer<T>): T {
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_12)) {
            location.world?.spawn(location, entity) { callback.accept(it) } ?: error("world is null")
        } else {
            val craftWorld = location.world as org.bukkit.craftbukkit.v1_12_R1.CraftWorld
            val nmsEntity = craftWorld.createEntity(location, entity)
            try {
                callback.accept(nmsEntity.bukkitEntity as T)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            craftWorld.addEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM)
        }
    }
}