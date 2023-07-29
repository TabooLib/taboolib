package taboolib.platform

import cn.nukkit.Server
import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.NukkitOpenContainer

/**
 * TabooLib
 * starslib.platform.NukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitOpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return Server.getInstance().pluginManager.plugins.values.filter { it.javaClass.name.endsWith("platform.NukkitPlugin") }.mapNotNull {
            pluginContainer.computeIfAbsent(it.name) { _ -> NukkitOpenContainer(it) }
        }
    }
}