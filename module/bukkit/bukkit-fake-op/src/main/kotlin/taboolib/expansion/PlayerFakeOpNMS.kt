package taboolib.expansion

import org.bukkit.entity.Player
import taboolib.common.Inject
import taboolib.common.env.RuntimeDependency
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.nmsProxy

@Inject
@RuntimeDependency(
    value = "!net.bytebuddy:byte-buddy:1.14.9",
    relocate = ["!net.bytebuddy", "!net.bytebuddy_1_14_9"],
    test = "!net.bytebuddy_1_14_9.ByteBuddy",
    transitive = false
)
abstract class PlayerFakeOpNMS {

    /**
     * 获取 [player] 的 PlayerFakeOp 代理对象
     *
     * 此代理对象使得 isOp() hasPermission(String) hasPermission(Permission) 三个方法均返回 true
     *
     * @param player 原 CraftPlayer 对象
     * @return [player] 的 PlayerFakeOp 代理对象
     */
    abstract fun playerFakeOp(player: Player): Player

    /**
     * 以 OP 权限执行指令
     *
     * 通过 NMS 创建带 OP 权限等级的 CommandSourceStack 直接在 Brigadier 层派发命令，
     * 不修改玩家真实 OP 状态，兼容 Paper 1.21.8+ 的 Brigadier 命令系统。
     * 1.12 及以下自动回退到 Bukkit.dispatchCommand(fakeOp(), cmd)。
     *
     * @param player 执行指令的玩家
     * @param command 指令内容（不含前缀 /）
     * @return 是否执行成功
     */
    abstract fun dispatchCommandAsOp(player: Player, command: String): Boolean

    companion object {

        val INSTANCE by unsafeLazy {
            nmsProxy<PlayerFakeOpNMS>()
        }
    }
}
