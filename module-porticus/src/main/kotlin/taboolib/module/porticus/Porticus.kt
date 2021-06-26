package taboolib.module.porticus

import com.google.common.collect.Lists
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide

/**
 * Porticus API 通用入口
 *
 * @author 坏黑
 * @since 2020-10-15
 */
@RuntimeDependency(value = "com.github.ben-manes.caffeine:caffeine:3.0.2", test = "com.github.benmanes.caffeine.cache.Cache")
@PlatformSide([Platform.BUKKIT, Platform.BUNGEE])
object Porticus {

    /**
     * 获取正在运行的通讯任务
     */
    val missions: List<PorticusMission> = Lists.newCopyOnWriteArrayList()

    /**
     * 获取 Porticus API
     */
    val API: API by lazy {
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