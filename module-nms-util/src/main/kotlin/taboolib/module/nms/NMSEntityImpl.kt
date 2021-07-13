package taboolib.module.nms

import net.minecraft.server.v1_16_R1.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import taboolib.common.Isolated
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.static
import taboolib.common.reflect.Reflex.Companion.unsafeInstance
import java.util.*

/**
 * @author sky
 * @since 2021/7/13 7:27 下午
 */
@Isolated
class NMSEntityImpl : NMSEntity {

    fun Player.sendPacket(packet: Any, vararg fields: Pair<String, Any?>) {
        sendPacket(setFields(packet, *fields))
    }

    fun setFields(any: Any, vararg fields: Pair<String, Any?>): Any {
        fields.forEach { (key, value) ->
            if (value != null) {
                any.reflex(key, value)
            }
        }
        return any
    }

    override fun spawnItem(player: Player, entityId: Int, uuid: UUID, location: Location, itemStack: ItemStack) {
        player.sendPacket(
            PacketPlayOutSpawnEntity::class.java.unsafeInstance(),
            "a" to entityId,
            "b" to uuid,
            "c" to location.x,
            "d" to location.y,
            "e" to location.z,
            "f" to (location.yaw * 256.0f / 360.0f).toInt().toByte(),
            "g" to (location.pitch * 256.0f / 360.0f).toInt().toByte(),
            "k" to if (MinecraftVersion.major <= 5) 2 else EntityTypes.ITEM
        )
    }

    override fun spawnArmorStand(player: Player, entityId: Int, uuid: UUID, location: Location) {
        val major = MinecraftVersion.major
        if (major < 5) {
            player.sendPacket(
                PacketPlayOutSpawnEntity::class.java.unsafeInstance(),
                "a" to entityId,
                "b" to uuid,
                "c" to location.x,
                "d" to location.y,
                "e" to location.z,
                "f" to (location.yaw * 256.0f / 360.0f).toInt().toByte(),
                "g" to (location.pitch * 256.0f / 360.0f).toInt().toByte(),
                "k" to 78
            )
        } else {
            player.sendPacket(
                PacketPlayOutSpawnEntityLiving::class.java,
                "a" to entityId,
                "b" to uuid,
                "c" to when {
                    major >= 6 -> IRegistry.ENTITY_TYPE.a(EntityTypes.ARMOR_STAND)
                    major == 5 -> net.minecraft.server.v1_13_R2.IRegistry.ENTITY_TYPE.a(net.minecraft.server.v1_13_R2.EntityTypes.ARMOR_STAND)
                    else -> 78
                },
                "d" to location.x,
                "e" to location.y,
                "f" to location.z,
                "g" to 0,
                "h" to 0,
                "i" to 0,
                "j" to (location.yaw * 256.0f / 360.0f).toInt().toByte(),
                "k" to (location.pitch * 256.0f / 360.0f).toInt().toByte(),
                "l" to (location.yaw * 256.0f / 360.0f).toInt().toByte(),
                "m" to if (major >= 7) null else DataWatcher(null)
            )
        }
    }

    override fun destroyEntity(player: Player, entityId: Int) {
        player.sendPacket(PacketPlayOutEntityDestroy::class.java.unsafeInstance(), "a" to arrayOf(entityId))
    }

    override fun teleportEntity(player: Player, entityId: Int, location: Location) {
        player.sendPacket(
            PacketPlayOutEntityTeleport::class.java.unsafeInstance(),
            "a" to entityId,
            "b" to location.x,
            "c" to location.y,
            "d" to location.z,
            "e" to (location.yaw * 256 / 360).toInt().toByte(),
            "f" to (location.pitch * 256 / 360).toInt().toByte(),
            "g" to false // onGround
        )
    }

    override fun updateEquipment(player: Player, entityId: Int, slot: EquipmentSlot, itemStack: ItemStack) {
        val major = MinecraftVersion.major
        when {
            major >= 8 -> {
                player.sendPacket(
                    PacketPlayOutEntityEquipment(
                        entityId,
                        listOf(com.mojang.datafixers.util.Pair(when (slot) {
                            EquipmentSlot.HAND -> EnumItemSlot.MAINHAND
                            EquipmentSlot.OFF_HAND -> EnumItemSlot.OFFHAND
                            EquipmentSlot.FEET -> EnumItemSlot.FEET
                            EquipmentSlot.LEGS -> EnumItemSlot.LEGS
                            EquipmentSlot.CHEST -> EnumItemSlot.CHEST
                            EquipmentSlot.HEAD -> EnumItemSlot.HEAD
                        }, CraftItemStack.asNMSCopy(itemStack)))
                    )
                )
            }
            major >= 1 -> {
                player.sendPacket(
                    net.minecraft.server.v1_13_R2.PacketPlayOutEntityEquipment(
                        entityId,
                        when (slot) {
                            EquipmentSlot.HAND -> net.minecraft.server.v1_13_R2.EnumItemSlot.MAINHAND
                            EquipmentSlot.OFF_HAND -> net.minecraft.server.v1_13_R2.EnumItemSlot.OFFHAND
                            EquipmentSlot.FEET -> net.minecraft.server.v1_13_R2.EnumItemSlot.FEET
                            EquipmentSlot.LEGS -> net.minecraft.server.v1_13_R2.EnumItemSlot.LEGS
                            EquipmentSlot.CHEST -> net.minecraft.server.v1_13_R2.EnumItemSlot.CHEST
                            EquipmentSlot.HEAD -> net.minecraft.server.v1_13_R2.EnumItemSlot.HEAD
                        },
                        org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack.asNMSCopy(itemStack)
                    )
                )
            }
            else -> {
                player.sendPacket(
                    net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment(
                        entityId,
                        when (slot) {
                            EquipmentSlot.HAND -> 0
                            EquipmentSlot.OFF_HAND -> 0
                            EquipmentSlot.FEET -> 1
                            EquipmentSlot.LEGS -> 2
                            EquipmentSlot.CHEST -> 3
                            EquipmentSlot.HEAD -> 4
                        },
                        org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(itemStack)
                    )
                )
            }
        }
    }

    override fun updateEntityMetadata(player: Player, entityId: Int, vararg objects: Any) {
        player.sendPacket(PacketPlayOutEntityMetadata::class.java.unsafeInstance(), "a" to entityId, "b" to objects.map { it as DataWatcher.Item<*> }.toList())
    }

    override fun getMetaEntityInt(index: Int, value: Int): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.b), value)
    }

    override fun getMetaEntityFloat(index: Int, value: Float): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.c), value)
    }

    override fun getMetaEntityString(index: Int, value: String): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.d), value)
    }

    override fun getMetaEntityBoolean(index: Int, value: Boolean): Any {
        return if (MinecraftVersion.major >= 5) {
            DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.i), value)
        } else {
            net.minecraft.server.v1_9_R2.DataWatcher.Item(
                net.minecraft.server.v1_9_R2.DataWatcherObject(
                    index,
                    net.minecraft.server.v1_9_R2.DataWatcherRegistry.h
                ), value
            )
        }
    }

    override fun getMetaEntityByte(index: Int, value: Byte): Any {
        return DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.a), value)
    }

    override fun getMetaEntityVector(index: Int, value: EulerAngle): Any {
        return if (MinecraftVersion.major >= 5) {
            DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.k), Vector3f(value.x.toFloat(), value.y.toFloat(), value.z.toFloat()))
        } else {
            net.minecraft.server.v1_12_R1.DataWatcher.Item(
                net.minecraft.server.v1_12_R1.DataWatcherObject(
                    index,
                    net.minecraft.server.v1_12_R1.DataWatcherRegistry.i
                ), net.minecraft.server.v1_12_R1.Vector3f(value.x.toFloat(), value.y.toFloat(), value.z.toFloat())
            )
        }
    }

    override fun getMetaEntityChatBaseComponent(index: Int, name: String?): Any {
        return if (MinecraftVersion.major >= 5) {
            DataWatcher.Item<Optional<IChatBaseComponent>>(
                DataWatcherObject(index, DataWatcherRegistry.f),
                Optional.ofNullable(if (name == null) null else CraftChatMessage.fromString(name).first())
            )
        } else {
            net.minecraft.server.v1_12_R1.DataWatcher.Item(
                net.minecraft.server.v1_12_R1.DataWatcherObject(
                    index,
                    net.minecraft.server.v1_12_R1.DataWatcherRegistry.d
                ), name ?: ""
            )
        }
    }

    override fun getMetaItem(index: Int, itemStack: ItemStack): Any {
        val major = MinecraftVersion.major
        return when {
            major >= 5 -> {
                DataWatcher.Item(DataWatcherObject(index, DataWatcherRegistry.g), CraftItemStack.asNMSCopy(itemStack))
            }
            major >= 4 -> {
                net.minecraft.server.v1_12_R1.DataWatcher.Item(
                    net.minecraft.server.v1_12_R1.DataWatcherObject(
                        6,
                        net.minecraft.server.v1_12_R1.DataWatcherRegistry.f
                    ), org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(itemStack)
                )
            }
            else -> {
                return net.minecraft.server.v1_9_R2.DataWatcher.Item(
                    net.minecraft.server.v1_9_R2.DataWatcherObject(
                        6,
                        net.minecraft.server.v1_9_R2.DataWatcherRegistry.f
                    ), com.google.common.base.Optional.fromNullable(org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack.asNMSCopy(itemStack))
                )
            }
        }
    }
}