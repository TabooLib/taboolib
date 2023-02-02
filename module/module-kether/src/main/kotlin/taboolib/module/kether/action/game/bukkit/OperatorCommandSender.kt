package taboolib.module.kether.action.game.bukkit

import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permission

/**
 * Adyeshach
 * taboolib.module.kether.action.game.bukkit.OperatorCommandSender
 *
 * @author 坏黑
 * @since 2023/2/2 23:42
 */
class OperatorCommandSender(val sender: CommandSender) : CommandSender by sender {

    override fun isOp(): Boolean {
        return true
    }

    override fun hasPermission(p0: Permission): Boolean {
        return true
    }

    override fun hasPermission(p0: String): Boolean {
        return true
    }
}