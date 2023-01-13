package taboolib.module.nms

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Proxy

/**
 * 使玩家强行执行指令 (AS-OP)
 * 原版指令的执行目前尚未兼容 (VanillaCommandWrapper)
 */
fun Player.executeCommands(vararg commands: Any) {
    val proxy = Proxy.newProxyInstance(
        Player::javaClass.javaClass.classLoader,
        arrayOf(Player::class.java)
    ) handler@{ _, method, args ->
        kotlin.runCatching {
            if (method.name.equals("hasPermission")) {
                return@handler true
            }
            if (args.isNullOrEmpty()) {
                return@handler method.invoke(this)
            }
            return@handler method.invoke(this, *args)
        }
    } as Player
    commands.forEach {
        Bukkit.getServer().dispatchCommand(proxy, it.toString())
    }
}