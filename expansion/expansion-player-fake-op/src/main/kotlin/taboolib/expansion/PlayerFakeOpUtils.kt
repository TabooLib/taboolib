package taboolib.expansion

import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * 获取 [this] 的 PlayerFakeOp 代理对象
 * 
 * 此代理对象使得 isOp() hasPermission(String) hasPermission(Permission) 三个方法均返回 true
 * 
 * @receiver 原 CraftPlayer 对象
 * @return [this] 的 PlayerFakeOp 代理对象
 */
fun Player.fakeOp(): Player = PlayerFakeOpNMS.INSTANCE.playerFakeOp(this)

/**
 * 以OP的权限执行指令 (仅针对非原版指令)
 */
fun Player.dispatchCommandAsOp(command: String): Boolean =
    Bukkit.dispatchCommand(fakeOp(), command)