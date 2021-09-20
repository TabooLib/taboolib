package taboolib.module.navigation

import net.minecraft.server.v1_11_R1.BlockTorch
import net.minecraft.server.v1_11_R1.IBlockAccess
import net.minecraft.server.v1_12_R1.BlockDoor
import net.minecraft.server.v1_12_R1.BlockPosition
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.entity.Entity
import taboolib.module.nms.MinecraftVersion

/**
 * Navigation
 * taboolib.module.navigation.NMSImpl
 *
 * @author sky
 * @since 2021/2/21 11:57 下午
 */
class NMSImpl : NMS() {

    val version = MinecraftVersion.major

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
        return if (version >= 11300) {
            if (block.type.isSolid) {
                (block.boundingBox.maxY - block.y).coerceAtLeast(0.0)
            } else {
                0.0
            }
        } else {
            when (version) {
                11200 -> {
                    val p = BlockPosition(block.x, block.y, block.z)
                    val b = (block.world as CraftWorld).handle.getType(p)
                    if (block.type.isSolid) {
                        b.d((block.world as CraftWorld).handle, p)?.e ?: 0.0
                    } else {
                        0.0
                    }
                }
                11100 -> {
                    val p = net.minecraft.server.v1_11_R1.BlockPosition(block.x, block.y, block.z)
                    val b = (block.world as org.bukkit.craftbukkit.v1_11_R1.CraftWorld).handle.getType(p)
                    (b.block as BlockTorch).a(b, (block.world as org.bukkit.craftbukkit.v1_11_R1.CraftWorld).handle as IBlockAccess, p)
                    if (block.type.isSolid) {
                        b.c((block.world as org.bukkit.craftbukkit.v1_11_R1.CraftWorld).handle, p)?.e ?: 0.0
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
                            b.c((block.world as org.bukkit.craftbukkit.v1_9_R2.CraftWorld).handle, p)?.e ?: 0.0
                        } else {
                            0.0
                        }
                    }
                }
            }
        }
    }

    override fun isDoorOpened(block: Block): Boolean {
        return (block.world as CraftWorld).handle.getType(BlockPosition(block.x, block.y, block.z)).get(BlockDoor.OPEN)
    }
}