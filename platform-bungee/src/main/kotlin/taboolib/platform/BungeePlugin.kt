package taboolib.platform

import net.md_5.bungee.api.plugin.Plugin
import java.io.File

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
class BungeePlugin : Plugin() {

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

        lateinit var instance: BungeePlugin
            private set
    }
}