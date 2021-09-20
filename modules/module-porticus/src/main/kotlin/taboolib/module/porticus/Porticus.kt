package taboolib.module.porticus

import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Porticus API 通用入口
 *
 * @author 坏黑
 * @since 2020-10-15
 */
@RuntimeDependency("!com.google.guava:guava:21.0", test = "!com.google.common.base.Optional")
@PlatformSide([Platform.BUKKIT, Platform.BUNGEE])
object Porticus {

    /**
     * 获取正在运行的通讯任务
     */
    val missions = CopyOnWriteArrayList<PorticusMission>()

    /**
     * 获取 Porticus API
     */
    val API by lazy {
        try {
            Class.forName("org.bukkit.Bukkit")
            return@lazy taboolib.module.porticus.bukkitside.PorticusAPI()
        } catch (ignored: Throwable) {
        }
        try {
            Class.forName("net.md_5.bungee.BungeeCord")
            return@lazy taboolib.module.porticus.bungeeside.PorticusAPI()
        } catch (ignored: Throwable) {
        }
        error("unsupported platform")
    }
}