package taboolib.module.navigation

import org.bukkit.World

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.util.ChunkAccess
 *
 * @author 坏黑
 * @since 2022/6/28 14:29
 */
abstract class ChunkAccess {

    abstract fun isChunkLoaded(world: World, chunkX: Int, chunkZ: Int): Boolean

    companion object {

        var instance = object : ChunkAccess() {

            override fun isChunkLoaded(world: World, chunkX: Int, chunkZ: Int): Boolean {
                return world.isChunkLoaded(chunkX, chunkZ)
            }
        }
    }
}