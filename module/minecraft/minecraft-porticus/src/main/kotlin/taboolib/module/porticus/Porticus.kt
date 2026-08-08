package taboolib.module.porticus

import net.md_5.bungee.BungeeCord
import org.bukkit.Bukkit
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.util.unsafeLazy
import taboolib.module.porticus.common.MessageReader
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Porticus API 通用入口
 *
 * @author 坏黑
 * @since 2020-10-15
 */
@Inject
@PlatformSide(Platform.BUKKIT, Platform.BUNGEE)
object Porticus {

    val channelId by unsafeLazy {
        "t_${if (pluginId.lowercase().length > 10) pluginId.lowercase().substring(0, 10) else pluginId.lowercase()}:main"
    }

    /**
     * 正在运行的通讯任务，按 UID 索引。
     *
     * 使用 Map 而非列表，令「同一 UID 只能有一个 pending 任务」成为结构约束，
     * 而不再依赖注册时的 O(n) 线性查重。
     *
     * 终态裁决（响应 / 超时 / 发送失败三方竞争）依赖 [MutableMap.remove] 的原子性：
     * `missions.remove(uid, mission)` 只会有一方拿到 true，从而保证回调恰好执行一次。
     */
    val missions = ConcurrentHashMap<UUID, PorticusMission>()

    /**
     * 获取 Porticus API
     */
    @JvmStatic
    lateinit var API: API
        private set

    /**
     * 因需要注册监听器，该方法不能在 onEnable 之前运行
     */
    @Awake(LifeCycle.ENABLE)
    private fun onEnable() {
        MessageReader.open()
        try {
            Bukkit.getServer()
            API = taboolib.module.porticus.bukkitside.PorticusAPI()
        } catch (ignored: Throwable) {
        }
        try {
            BungeeCord.getInstance()
            API = taboolib.module.porticus.bungeeside.PorticusAPI()
        } catch (ignored: Throwable) {
        }
    }

    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        missions.clear()
        MessageReader.close()
    }
}