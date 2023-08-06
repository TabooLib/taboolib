package taboolib.module.nms.v2

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.entity.CreatureSpawnEvent
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.nmsProxy
import taboolib.module.nms.type.LocaleKey
import java.util.function.Consumer

/**
 *  在坐标处中生成实体，并在生成前执行回调函数
 */
fun <T : Entity> Location.spawnEntity(entity: Class<T>, prepare: Consumer<T>): T {
    return nmsProxy<NMSEntity>().spawnEntity(this, entity, prepare)
}

/**
 * 获取实体的语言文件节点
 */
fun Entity.getLocaleKey(): LocaleKey {
    return nmsProxy<NMSEntity>().getLocaleKey(this)
}

/**
 * TabooLib
 * taboolib.module.nms.NMSEntity
 *
 * @author 坏黑
 * @since 2023/8/5 03:47
 */
abstract class NMSEntity {

    /** 在坐标处中生成实体，并在生成前执行回调函数 */
    abstract fun <T : Entity> spawnEntity(location: Location, entity: Class<T>, prepare: Consumer<T>): T

    /** 获取实体类型 */
    abstract fun getEntityType(name: String): Any

    /** 获取实体语言文件节点 */
    abstract fun getLocaleKey(entity: Entity?): LocaleKey
}

/**
 * [NMSEntity] 的实现类
 */
class NMSEntityImpl : NMSEntity() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> spawnEntity(location: Location, entity: Class<T>, prepare: Consumer<T>): T {
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_12)) {
            location.world?.spawn(location, entity) { prepare.accept(it) } ?: error("world is null")
        } else {
            val craftWorld = location.world as org.bukkit.craftbukkit.v1_12_R1.CraftWorld
            val nmsEntity = craftWorld.createEntity(location, entity)
            try {
                prepare.accept(nmsEntity.bukkitEntity as T)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            craftWorld.addEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM)
        }
    }
    
    override fun getEntityType(name: String): Any {
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_14)) {
            net.minecraft.server.v1_14_R1.EntityTypes.a(name).orElse(null)
        } else {
            net.minecraft.server.v1_13_R2.EntityTypes.a(name)!!
        }
    }

    override fun getLocaleKey(entity: Entity?): LocaleKey {
        TODO("Not yet implemented")
    }
}