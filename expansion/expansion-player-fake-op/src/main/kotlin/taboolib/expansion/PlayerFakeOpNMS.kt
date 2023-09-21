package taboolib.expansion

import org.bukkit.entity.Player
import taboolib.common.env.RuntimeDependency
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.nmsProxy

@RuntimeDependency(
    value = "!net.bytebuddy:byte-buddy:1.14.6",
    relocate = ["!net.bytebuddy", "!net.bytebuddy_1_14_6"],
    test = "!net.bytebuddy_1_14_6.ByteBuddy",
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
    
    companion object {
        val INSTANCE by unsafeLazy { 
            nmsProxy<PlayerFakeOpNMS>()
        }
    }
    
}