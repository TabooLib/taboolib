package taboolib.platform

import cn.nukkit.plugin.PluginBase
import java.io.File

/**
 * TabooLib
 * taboolib.platform.NukkitPlugin
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
class NukkitPlugin : PluginBase() {

    init {
        instance = this
    }

    override fun onLoad() {
    }

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    public override fun getFile(): File {
        return super.getFile()
    }

    companion object {

        lateinit var instance: NukkitPlugin
            private set
    }
}