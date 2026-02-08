package taboolib.module.nms

import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.Translatable
import org.bukkit.entity.Entity
import org.bukkit.entity.Villager
import org.bukkit.event.entity.CreatureSpawnEvent
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.UnsupportedVersionException
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.MinecraftLanguage.LanguageKey.Type
import java.util.function.Consumer

/**
 *  在坐标处中生成实体，并在生成前执行回调函数
 */
fun <T : Entity> Location.spawnEntity(entity: Class<T>, prepare: Consumer<T> = Consumer { }): T {
    return NMSEntity.instance.spawnEntity(this, entity, prepare)
}

/**
 * 获取实体的语言文件节点
 */
fun Entity.getLanguageKey(): MinecraftLanguage.LanguageKey {
    return NMSEntity.instance.getLanguageKey(this)
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
    abstract fun <T : Entity> spawnEntity(location: Location, entity: Class<T>, callback: Consumer<T>): T

    /** 获取实体语言文件节点 */
    abstract fun getLanguageKey(entity: Entity): MinecraftLanguage.LanguageKey

    companion object {

        val instance by unsafeLazy { nmsProxy<NMSEntity>() }
    }
}

// region NMSEntityImpl
class NMSEntityImpl : NMSEntity() {

    /**
     * 是否支持 Translatable
     */
    val isTranslatableSupported  by lazy {
        if (MinecraftVersion.versionId >= 11903) {
            runCatching { Class.forName("org.bukkit.Translatable") }.isSuccess
        } else false
    }

    /**
     * 1.19.3, 1.20 -> BuiltInRegistries.VILLAGER_PROFESSION
     */
    val villagerProfessionBuiltInRegistries by unsafeLazy { nmsClass("BuiltInRegistries").getProperty<Any>("VILLAGER_PROFESSION", isStatic = true)!! }

    /**
     * 1.17, 1.19.2 -> IRegistry.VILLAGER_PROFESSION
     */
    val villagerProfessionIRegistry by unsafeLazy { nmsClass("IRegistry").getProperty<Any>("VILLAGER_PROFESSION", isStatic = true)!! }

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

    @Suppress("SpellCheckingInspection")
    override fun getLanguageKey(entity: Entity): MinecraftLanguage.LanguageKey {
        val key = when (MinecraftVersion.major) {
            // region 1.17 .. 1.21
            in MinecraftVersion.V1_17..MinecraftVersion.V1_21 -> {
                // 使用 Translatable 接口
                if (isTranslatableSupported) {
                    var key = entity.type.translationKey
                    if (entity is Villager) {
                        key += "." + entity.invokeMethod<Keyed>("getProfession")!!.key.key
                    }
                    key
                } else {
                    entity as org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity
                    val nmsEntity = entity.handle
                    var key = when {
                        // 1.17 版本特殊处理
                        MinecraftVersion.isEqual(MinecraftVersion.V1_17) -> nmsEntity.type.invokeMethod<String>("g", remap = true)!!
                        else -> nmsEntity.type.descriptionId
                    }
                    // 对村民特殊处理
                    if (nmsEntity is net.minecraft.world.entity.npc.EntityVillager) {
                        key += "." + getVillagerLocaleKey3(nmsEntity)
                    }
                    key
                }
            }
            // endregion
            // region 1.14 .. 1.16
            in MinecraftVersion.V1_14..MinecraftVersion.V1_16 -> {
                entity as org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
                val nmsEntity = entity.handle
                when {
                    // 对村民特殊处理
                    nmsEntity is net.minecraft.server.v1_16_R3.EntityVillager -> getVillagerLocaleKey2(nmsEntity)
                    // 其他实体
                    else -> nmsEntity.entityType.f()
                }
            }
            // endregion
            // region 1.13
            MinecraftVersion.V1_13 -> {
                entity as org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity
                val nmsEntity = entity.handle
                when {
                    // 对村民特殊处理
                    nmsEntity is net.minecraft.server.v1_13_R2.EntityVillager -> getVillagerLocaleKey1(nmsEntity)
                    // 其他实体
                    else -> nmsEntity.P().d()
                }
            }
            // endregion
            // region 1.8 .. 1.12
            in MinecraftVersion.V1_8..MinecraftVersion.V1_12 -> {
                entity as org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
                val nmsEntity = entity.handle
                when {
                    // 对马特殊处理，真逆天啊。。。
                    // 1.11, 1.12 版本不再使用这种方式
                    nmsEntity is net.minecraft.server.v1_8_R3.EntityHorse && MinecraftVersion.isLower(MinecraftVersion.V1_10) -> when (nmsEntity.type) {
                        1 -> "entity.donkey.name"
                        2 -> "entity.mule.name"
                        3 -> "entity.zombiehorse.name"
                        4 -> "entity.skeletonhorse.name"
                        else -> "entity.horse.name"
                    }
                    // 对村民特殊处理，更逆天的在这里。。。
                    nmsEntity is net.minecraft.server.v1_8_R3.EntityVillager -> getVillagerLocaleKey0(nmsEntity)
                    // 其他实体
                    else -> "entity.${net.minecraft.server.v1_8_R3.EntityTypes.b(nmsEntity) ?: "generic"}.name"
                }
            }
            // endregion
            // 不支持的版本
            else -> throw UnsupportedVersionException()
        }
        return MinecraftLanguage.LanguageKey(Type.NORMAL, key)
    }

    /**
     * 1.8 .. 1.12 获取村民的语言文件节点
     */
    private fun getVillagerLocaleKey0(nmsEntity: Any): String {
        nmsEntity as net.minecraft.server.v1_8_R3.EntityVillager
        // 因为 this.bx 是私有字段，只能通过导出的 NBTTagCompound 来获取
        val export = net.minecraft.server.v1_8_R3.NBTTagCompound()
        nmsEntity.b(export)
        val career = export.getInt("Career")
        // region 职业获取
        // 在马的逆天写法移除后，村民的这种写法竟然一直用到 1.13 版本结束
        val type = when (nmsEntity.profession) {
            0 -> when (career) {
                1 -> "farmer"
                2 -> "fisherman"
                3 -> "shepherd"
                4 -> "fletcher"
                else -> null
            }

            1 -> when (career) {
                2 -> "cartographer" // 1.11+ 制图师
                else -> "librarian"
            }

            2 -> "cleric"
            3 -> when (career) {
                1 -> "armor"
                2 -> "weapon"
                3 -> "tool"
                else -> null
            }

            4 -> when (career) {
                1 -> "butcher"
                2 -> "leather"
                else -> null
            }

            5 -> "nitwit" // 1.11+ 傻子
            else -> null
        }
        // endregion
        return "entity.Villager.${type ?: "name"}"
    }

    /**
     * 1.13 获取村民的语言文件节点
     */
    @Suppress("SpellCheckingInspection")
    private fun getVillagerLocaleKey1(nmsEntity: Any): String {
        nmsEntity as net.minecraft.server.v1_13_R2.EntityVillager
        val export = net.minecraft.server.v1_13_R2.NBTTagCompound()
        nmsEntity.b(export)
        val career = export.getInt("Career")
        // region 职业获取
        val type = when (nmsEntity.profession) {
            0 -> when (career) {
                1 -> "farmer"
                2 -> "fisherman"
                3 -> "shepherd"
                4 -> "fletcher"
                else -> null
            }

            1 -> when (career) {
                1 -> "librarian"
                2 -> "cartographer"
                else -> null
            }

            2 -> "cleric"
            3 -> when (career) {
                1 -> "armorer"
                2 -> "weapon_smith"
                3 -> "tool_smith"
                else -> null
            }

            4 -> when (career) {
                1 -> "butcher"
                2 -> "leatherworker"
                else -> null
            }

            5 -> "nitwit"
            else -> null
        }
        // endregion
        return if (type != null) "entity.minecraft.villager.$type" else "entity.minecraft.villager"
    }

    /**
     * 1.14 .. 1.16 获取村民的语言文件节点
     */
    private fun getVillagerLocaleKey2(nmsEntity: Any): String {
        // 终于没那么逆天了
        nmsEntity as net.minecraft.server.v1_14_R1.EntityVillager
        return nmsEntity.entityType.f() + "." + net.minecraft.server.v1_14_R1.IRegistry.VILLAGER_PROFESSION.getKey(nmsEntity.villagerData.profession).key
    }

    /**
     * 1.17 .. 1.20 获取村民的语言文件节点（只获取职业节点）
     */
    @Suppress("UNCHECKED_CAST")
    private fun getVillagerLocaleKey3(nmsEntity: Any): String {
        nmsEntity as net.minecraft.world.entity.npc.EntityVillager
        val registry = runCatching { villagerProfessionBuiltInRegistries }.getOrElse { villagerProfessionIRegistry }
        registry as net.minecraft.core.IRegistry<Any>
        return registry.getKey(nmsEntity.villagerData.profession)!!.getProperty<String>("path")!!
    }
}
// endregion