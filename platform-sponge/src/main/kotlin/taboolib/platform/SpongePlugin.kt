package taboolib.platform

import com.google.inject.Inject
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameConstructionEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStartedServerEvent
import org.spongepowered.api.event.game.state.GameStoppingServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import java.io.File

/**
 * TabooLib
 * taboolib.platform.SpongePlugin
 *
 * @author sky
 * @since 2021/6/15 1:54 上午
 */
@Plugin(id = "@plugin_id", name = "@plugin_name", version = "@plugin_version")
class SpongePlugin {

    @Inject
    lateinit var pluginContainer: PluginContainer

    @Inject
    @ConfigDir(sharedRoot = false)
    lateinit var pluginConfigDir: File

    @Listener
    fun e(e: GameConstructionEvent) {
    }

    @Listener
    fun e(e: GameInitializationEvent) {
    }

    @Listener
    fun e(e: GameStartedServerEvent) {
    }

    @Listener
    fun e(e: GameStoppingServerEvent) {
    }

    init {
        instance = this
    }

    companion object {

        lateinit var instance: SpongePlugin
            private set
    }
}