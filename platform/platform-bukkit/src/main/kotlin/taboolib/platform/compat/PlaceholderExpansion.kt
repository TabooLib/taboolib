@file:Isolated

package taboolib.platform.compat

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.events.ExpansionUnregisterEvent
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.submit
import taboolib.common.util.unsafeLazy
import taboolib.platform.BukkitPlugin
import java.util.function.Supplier

fun String.replacePlaceholder(player: Player): String {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun List<String>.replacePlaceholder(player: Player): List<String> {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

/**
 * TabooLib
 * taboolib.platform.compat.PlaceholderExpansion
 *
 * @author sky
 * @since 2021/7/20 1:53 下午
 */
@Isolated
interface PlaceholderExpansion {

    val identifier: String

    /**
     * 是否启用
     */
    val enabled: Boolean
        get() = true

    val autoReload: Boolean
        get() = false

    fun onPlaceholderRequest(player: Player?, args: String): String {
        return "onPlaceholderRequest(player: Player?, args: String) not implemented"
    }

    fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        if (player?.isOnline == true) {
            return onPlaceholderRequest(player.player, args)
        }
        return "onPlaceholderRequest(player: OfflinePlayer?, args: String) not implemented"
    }

    @Awake
    class PlaceholderRegister : ClassVisitor(0) {

        val hooked by unsafeLazy {
            kotlin.runCatching { Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion") }.isSuccess
        }

        override fun visitStart(clazz: Class<*>, instance: Supplier<*>?) {
            if (hooked && clazz.interfaces.contains(PlaceholderExpansion::class.java)) {
                val expansion = instance?.get() as? PlaceholderExpansion ?: error("PlaceholderExpansion must have an instance")
                if (!expansion.enabled) {
                    return
                }
                object : me.clip.placeholderapi.expansion.PlaceholderExpansion() {

                    override fun persist(): Boolean {
                        return true
                    }

                    override fun getIdentifier(): String {
                        return expansion.identifier
                    }

                    override fun getAuthor(): String {
                        return BukkitPlugin.getInstance().description.authors.toString()
                    }

                    override fun getVersion(): String {
                        return BukkitPlugin.getInstance().description.version
                    }

                    override fun onPlaceholderRequest(player: Player?, params: String): String {
                        return expansion.onPlaceholderRequest(player, params)
                    }

                    override fun onRequest(player: OfflinePlayer?, params: String): String {
                        return expansion.onPlaceholderRequest(player, params)
                    }
                }.also { papiExpansion ->
                    // 自动重载
                    if (expansion.autoReload) {
                        registerBukkitListener(ExpansionUnregisterEvent::class.java) {
                            if (it.expansion == papiExpansion) {
                                submit { papiExpansion.register() }
                            }
                        }
                    }
                }.register()
            }
        }

        override fun getLifeCycle(): LifeCycle {
            return LifeCycle.ENABLE
        }
    }
}