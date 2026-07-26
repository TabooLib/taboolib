package taboolib.platform.type

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandManager
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.console.ConsoleSender
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import taboolib.common.PrimitiveIO
import taboolib.common.platform.ProxyCommandSender
import java.util.WeakHashMap
import java.util.concurrent.CompletableFuture

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

        @JvmSynthetic
        internal fun dispatchCommand(dispatch: () -> CompletableFuture<Void>): Boolean {
            // 不阻塞等待执行结果：命令处理线程上等待命令处理 Future 会形成自死锁。
            // 代价是返回值恒为 true，无法反映命令是否真正执行成功；
            // 因此这里挂上回调，至少让执行失败在控制台可见。
            try {
                dispatch().whenComplete { _, ex ->
                    if (ex != null) {
                        PrimitiveIO.error("Failed to dispatch command: {0}", ex.message ?: ex.javaClass.name)
                        ex.printStackTrace()
                    }
                }
            } catch (ex: Throwable) {
                PrimitiveIO.error("Failed to dispatch command: {0}", ex.message ?: ex.javaClass.name)
                ex.printStackTrace()
            }
            return true
        }

        private val quitLock = Any()
        private val quitCallbacks = WeakHashMap<Any, LinkedHashSet<Runnable>>()
        private val completedQuitSessions = WeakHashMap<Any, Boolean>()

        /**
         * 激活会话，清除该会话的「已完成」标记。
         *
         * 仅应在玩家真正进入服务器时调用。正常路径下由 [registerQuitCallback]
         * 在玩家在线时自动完成，该方法供平台在明确得知玩家进入服务器时主动调用。
         */
        @JvmSynthetic
        internal fun activateQuitSession(session: Any) {
            synchronized(quitLock) {
                completedQuitSessions.remove(session)
            }
        }

        /**
         * 注册退出回调。
         *
         * @param session 会话标识（`player.playerRef`）
         * @param online  注册时玩家是否仍在线。玩家在线说明这是一个新会话，
         *                此时需要清除同一 session 上遗留的「已完成」标记，
         *                否则重连后注册的回调会被立即执行。
         *                该判断放在注册时而非包装实例的构造函数中，
         *                因为 `adaptPlayer` 会反复构造实例，在构造函数中重置会把退出终态覆盖掉。
         */
        @JvmSynthetic
        internal fun registerQuitCallback(session: Any, callback: Runnable, online: Boolean = false) {
            val runImmediately = synchronized(quitLock) {
                if (online) {
                    completedQuitSessions.remove(session)
                }
                if (completedQuitSessions.containsKey(session)) {
                    true
                } else {
                    quitCallbacks.getOrPut(session) { LinkedHashSet() }.add(callback)
                    false
                }
            }
            if (runImmediately) {
                callback.run()
            }
        }

        @JvmSynthetic
        internal fun fireQuitCallbacks(session: Any) {
            val registered = synchronized(quitLock) {
                completedQuitSessions[session] = true
                quitCallbacks.remove(session)?.toList().orEmpty()
            }
            // 该方法由平台事件回调触发，抛出异常可能中断后续监听器，因此仅记录不抛出。
            registered.forEach {
                try {
                    it.run()
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }

        @JvmSynthetic
        internal fun clearQuitCallbacks() {
            synchronized(quitLock) {
                quitCallbacks.clear()
                completedQuitSessions.clear()
            }
        }
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
        return dispatchCommand { CommandManager.get().handleCommand(sender, command) }
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
            return dispatchCommand { CommandManager.get().handleCommand(console, command) }
        }

        override fun hasPermission(permission: String): Boolean {
            return console.hasPermission(permission)
        }
    }
}
