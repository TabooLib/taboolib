package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.*
import taboolib.common.platform.service.PlatformCommand
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.SpongeCommand
 *
 * @author sky
 * @since 2021/7/4 2:45 下午
 */
@Internal
@Awake
@PlatformSide([Platform.SPONGE_API_8])
class Sponge8Command : PlatformCommand {

    override fun registerCommand(
        command: CommandInfo,
        executor: CommandExecutor,
        completer: CommandCompleter,
        component: Component.() -> Unit
    ) {
        // TODO: 2021/7/15 Not Support
    }

    override fun unregisterCommand(command: String) {
    }

    override fun unregisterCommands() {
    }
}