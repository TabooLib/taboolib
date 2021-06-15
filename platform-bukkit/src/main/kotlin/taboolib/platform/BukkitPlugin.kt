package taboolib.platform

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * TabooLib
 * taboolib.platform.BukkitPlugin
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
class BukkitPlugin : JavaPlugin() {

    override fun onLoad() {
    }

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator? {
        return null
    }

    public override fun getFile(): File {
        return super.getFile()
    }
}