package taboolib.platform

import net.md_5.bungee.api.plugin.Plugin
import taboolib.common.TabooLibCommon
import taboolib.common.io.findInstance
import taboolib.common.platform.startExecutor
import taboolib.common.platform.submit

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
class BungeePlugin : Plugin() {

    val pluginInstance: taboolib.plugin.Plugin?

    init {
        TabooLibCommon.init()
        instance = this
        pluginInstance = findInstance(taboolib.plugin.Plugin::class.java)
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

    companion object {

        lateinit var instance: BungeePlugin
            private set
    }
}