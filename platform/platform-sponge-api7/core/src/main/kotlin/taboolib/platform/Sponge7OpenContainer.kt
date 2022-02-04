package taboolib.platform

import org.spongepowered.api.Sponge
import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.Sponge7OpenContainer

/**
 * TabooLib
 * taboolib.platform.SpongeIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7OpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return Sponge.getPluginManager().plugins.filter { it.instance.orElse(null)?.javaClass?.name?.endsWith("platform.Sponge7Plugin") == true }.mapNotNull {
            pluginContainer.computeIfAbsent(it.id) { _ -> Sponge7OpenContainer(it) }
        }
    }
}