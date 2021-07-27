package taboolib.platform

import org.bukkit.generator.ChunkGenerator

/**
 * TabooLib
 * taboolib.platform.BukkitWorldGenerator
 *
 * @author sky
 * @since 2021/6/16 9:39 下午
 */
interface BukkitWorldGenerator {

    fun getDefaultWorldGenerator(worldName: String, name: String?): ChunkGenerator?
}