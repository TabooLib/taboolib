package taboolib.module.navigation

import net.minecraft.server.v1_12_R1.BlockDoor
import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.world.level.block.state.IBlockData
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.entity.Entity
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.MinecraftVersion.isHigherOrEqual

/**
 * Navigation
 * taboolib.module.navigation.NMSImpl
 *
 * @author sky
 * @since 2021/2/21 11:57 下午
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate", "unused")
class NMSImpl : NMS() {

    val version = MinecraftVersion.major
    val majorLegacy = MinecraftVersion.versionId

    override fun getBoundingBox(entity: Entity): BoundingBox {
        return if (version >= 5) {
            entity.boundingBox.run { BoundingBox(minX, minY, minZ, maxX, maxY, maxZ) }
        } else {
            (entity as CraftEntity).handle.boundingBox.run { BoundingBox(a, b, c, d, e, f) }
        }
    }

    override fun getBoundingBox(block: Block): BoundingBox {
        return if (version >= 5) {
            block.boundingBox.run { BoundingBox(minX, minY, minZ, maxX, maxY, maxZ) }
        } else {
            val blockPosition = BlockPosition(block.x, block.y, block.z)
            val worldServer = (block.world as CraftWorld).handle
            val blockData = worldServer.getType(blockPosition)
            val ab = blockData.d(worldServer, blockPosition)
            if (ab == null) {
                BoundingBox.zero()
            } else {
                BoundingBox(ab.a + block.x, ab.b + block.y, ab.c + block.z, ab.d + block.x, ab.e + block.y, ab.f + block.z)
            }
        }
    }

    override fun getBlockHeight(block: Block): Double {
        return if (isHigherOrEqual(MinecraftVersion.V1_13)) {
            if (block.type.isSolid) {
                (block.boundingBox.maxY - block.y).coerceAtLeast(0.0)
            } else {
                0.0
            }
        } else {
            when {
                majorLegacy > 11200 -> {
                    val p = BlockPosition(block.x, block.y, block.z)
                    val b = (block.world as CraftWorld).handle.getType(p)
                    if (block.type.isSolid) {
                        val a = b.d((block.world as CraftWorld).handle, p)
                        a?.e ?: 0.0
                    } else {
                        0.0
                    }
                }
                majorLegacy > 11100 -> {
                    val p = net.minecraft.server.v1_11_R1.BlockPosition(block.x, block.y, block.z)
                    val b = (block.world as org.bukkit.craftbukkit.v1_11_R1.CraftWorld).handle.getType(p)
                    if (block.type.isSolid) {
                        val a = b.c((block.world as org.bukkit.craftbukkit.v1_11_R1.CraftWorld).handle, p)
                        a?.e ?: 0.0
                    } else {
                        0.0
                    }
                }
                else -> {
                    if (block.isEmpty) {
                        0.0
                    } else {
                        val p = net.minecraft.server.v1_9_R2.BlockPosition(block.x, block.y, block.z)
                        val b = (block.world as org.bukkit.craftbukkit.v1_9_R2.CraftWorld).handle.getType(p)
                        if (block.type.isSolid) {
                            val a = b.c((block.world as org.bukkit.craftbukkit.v1_9_R2.CraftWorld).handle, p)
                            a?.e ?: 0.0
                        } else {
                            0.0
                        }
                    }
                }
            }
        }
    }
    override fun isDoorOpened(block: Block): Boolean {
        return when {
            // 1.18 起函数名发生变动: getType -> getBlockState
            isHigherOrEqual(MinecraftVersion.V1_18) -> {
                (block.world as org.bukkit.craftbukkit.v1_21_R1.CraftWorld).handle
                    .getBlockState(net.minecraft.core.BlockPosition(block.x, block.y, block.z))
                    .getValue(net.minecraft.world.level.block.BlockDoor.OPEN)
            }
            isHigherOrEqual(MinecraftVersion.V1_17) -> {
                (block.world as org.bukkit.craftbukkit.v1_16_R3.CraftWorld).handle
                    .invokeMethod<IBlockData>("getType", net.minecraft.core.BlockPosition(block.x, block.y, block.z))!!
                    .invokeMethod("get", net.minecraft.world.level.block.BlockDoor::class.java.getProperty<Any>("OPEN", isStatic = true))!!
            }
            // 1.14 (v1_14_R1) 中该 IBlockData 类由 interface 变为 class
            isHigherOrEqual(MinecraftVersion.V1_14) -> {
                (block.world as org.bukkit.craftbukkit.v1_14_R1.CraftWorld).handle
                    .getType(net.minecraft.server.v1_14_R1.BlockPosition(block.x, block.y, block.z))
                    .get(net.minecraft.server.v1_14_R1.BlockDoor.OPEN)
            }
            // 1.13 及以下版本
            else -> {
                (block.world as CraftWorld).handle.getType(BlockPosition(block.x, block.y, block.z)).get(BlockDoor.OPEN)
            }
        }
    }
}