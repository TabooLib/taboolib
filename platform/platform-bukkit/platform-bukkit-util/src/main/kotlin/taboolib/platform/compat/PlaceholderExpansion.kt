@file:Isolated
package taboolib.platform.compat

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
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

    fun onPlaceholderRequest(player: Player?, args: String): String

    @Awake
    object PlaceholderRegister : Injector.Classes {

        val hooked by lazy {
            kotlin.runCatching { Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion") }.isSuccess
        }

        override fun inject(clazz: Class<*>, instance: Supplier<*>) {
            if (hooked && clazz.interfaces.contains(PlaceholderExpansion::class.java)) {
                val expansion = instance.get() as PlaceholderExpansion
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

                    override fun onPlaceholderRequest(player: Player, params: String): String {
                        return expansion.onPlaceholderRequest(player, params)
                    }
                }.register()
            }
        }

        override fun postInject(clazz: Class<*>, instance: Supplier<*>) {
        }

        override val lifeCycle: LifeCycle
            get() = LifeCycle.ENABLE

        override val priority: Byte
            get() = 0
    }
}