package taboolib.platform

import cn.nukkit.plugin.PluginBase
import taboolib.common.TabooLibCommon
import taboolib.common.io.findInstance
import taboolib.common.platform.startExecutor
import taboolib.common.platform.submit
import taboolib.plugin.Plugin
import java.io.File

/**
 * TabooLib
 * taboolib.platform.NukkitPlugin
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
class NukkitPlugin : PluginBase() {

    val pluginInstance: Plugin?

    init {
        TabooLibCommon.init()
        instance = this
        pluginInstance = findInstance(Plugin::class.java)
    }

    override fun onLoad() {
        pluginInstance?.onLoad()
    }

    override fun onEnable() {
        pluginInstance?.onEnable()
        startExecutor()
        submit {
            pluginInstance?.onActive()
        }
    }

    override fun onDisable() {
        pluginInstance?.onDisable()
        TabooLibCommon.cancel()
    }

    public override fun getFile(): File {
        return super.getFile()
    }


    companion object {

        lateinit var instance: NukkitPlugin
            private set
    }

}