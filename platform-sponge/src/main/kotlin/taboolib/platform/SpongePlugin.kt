package taboolib.platform

import com.google.inject.Inject
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameConstructionEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStartedServerEvent
import org.spongepowered.api.event.game.state.GameStoppingServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import taboolib.common.TabooLibCommon
import taboolib.common.io.findInstance
import taboolib.common.platform.startExecutor
import java.io.File

/**
 * TabooLib
 * taboolib.platform.SpongePlugin
 *
 * @author sky
 * @since 2021/6/15 1:54 上午
 */
@Plugin(id = "@plugin_id@", name = "@plugin_name@", version = "@plugin_version@")
class SpongePlugin {

    val pluginInstance: taboolib.plugin.Plugin?

    @Inject
    lateinit var pluginContainer: PluginContainer

    @Inject
    @ConfigDir(sharedRoot = false)
    lateinit var pluginConfigDir: File

    init {
        TabooLibCommon.init()
        instance = this
        pluginInstance = findInstance(taboolib.plugin.Plugin::class.java)
    }

    @Listener
    fun e(e: GameConstructionEvent) {
        pluginInstance?.onLoad()
    }

    @Listener
    fun e(e: GameInitializationEvent) {
        pluginInstance?.onEnable()
        startExecutor()
    }

    @Listener
    fun e(e: GameStartedServerEvent) {
        pluginInstance?.onActive()
    }

    @Listener
    fun e(e: GameStoppingServerEvent) {
        pluginInstance?.onDisable()
        TabooLibCommon.cancel()
    }

    companion object {

        lateinit var instance: SpongePlugin
            private set
    }
}