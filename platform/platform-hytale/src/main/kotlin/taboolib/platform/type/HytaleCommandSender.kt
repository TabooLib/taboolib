package taboolib.platform.type

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandManager
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.console.ConsoleSender
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.platform.type.HytaleCommandSender
 *
 * @author sky
 * @since 2024/1/1
 */
open class HytaleCommandSender(val sender: CommandSender) : ProxyCommandSender {

    companion object {
        /** 移除 Minecraft 颜色代码 (§x) */
        private val COLOR_PATTERN = Regex("§.")
        
        fun stripColor(message: String): String = message.replace(COLOR_PATTERN, "")
    }

    override val origin: Any
        get() = sender

    override val name: String
        get() = sender.displayName

    override var isOp: Boolean
        get() {
            val uuid = sender.uuid ?: return false
            return PermissionsModule.get().getGroupsForUser(uuid).contains("OP")
        }
        set(value) {
            val uuid = sender.uuid ?: return
            if (value) {
                PermissionsModule.get().addUserToGroup(uuid, "OP")
            } else {
                PermissionsModule.get().removeUserFromGroup(uuid, "OP")
            }
        }

    override fun isOnline(): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        sender.sendMessage(Message.raw(stripColor(message)))
    }

    override fun performCommand(command: String): Boolean {
        val future = CommandManager.get().handleCommand(sender, command)
        return try {
            future.get()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

    /**
     * 控制台发送者 - 使用 Hytale 原生 ConsoleSender
     */
    object Console : ProxyCommandSender {

        private val console: ConsoleSender
            get() = ConsoleSender.INSTANCE

        override val origin: Any
            get() = console

        override val name: String
            get() = console.displayName

        override var isOp: Boolean
            get() = true // Console 始终拥有 OP 权限
            set(_) {}

        override fun isOnline(): Boolean {
            return true
        }

        override fun sendMessage(message: String) {
            console.sendMessage(Message.raw(stripColor(message)))
        }

        override fun performCommand(command: String): Boolean {
            val future = CommandManager.get().handleCommand(console, command)
            return try {
                future.get()
                true
            } catch (e: Exception) {
                false
            }
        }

        override fun hasPermission(permission: String): Boolean {
            return console.hasPermission(permission)
        }
    }
}
