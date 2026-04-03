package taboolib.expansion

import org.bukkit.entity.Player

/**
 * 获取 [this] 的 PlayerFakeOp 代理对象
 *
 * 此代理对象使得 isOp() hasPermission(String) hasPermission(Permission) 三个方法均返回 true
 *
 * @receiver 原 CraftPlayer 对象
 * @return [this] 的 PlayerFakeOp 代理对象
 */
fun Player.fakeOp(): Player {
    return PlayerFakeOpNMS.INSTANCE.playerFakeOp(this)
}

/**
 * 以 OP 权限执行指令
 *
 * 通过 NMS 直接在 Brigadier 层同步派发命令，不修改玩家真实 OP 状态：
 * - NMS 层：CommandSourceStack 使用 ALL_PERMISSIONS（处理原版命令权限检查）
 * - Bukkit 层：替换 CommandSource 使 getBukkitSender() 返回 FakeOp 代理（处理插件命令权限检查）
 */
fun Player.dispatchCommandAsOp(command: String): Boolean {
    return PlayerFakeOpNMS.INSTANCE.dispatchCommandAsOp(this, command)
}