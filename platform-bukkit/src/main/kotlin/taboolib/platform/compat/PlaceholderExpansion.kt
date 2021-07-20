package taboolib.platform.compat

import org.bukkit.entity.Player
import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.platform.BukkitPlugin

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

    fun onPlaceholderRequest(player: Player, args: String): String

    @Awake
    object PlaceholderRegister : Injector.Classes {

        override fun inject(clazz: Class<*>, instance: Any) {
            if (clazz.interfaces.contains(PlaceholderExpansion::class.java)) {
                PlaceholderProxy(instance as PlaceholderExpansion).register()
            }
        }

        override val lifeCycle: LifeCycle
            get() = LifeCycle.ENABLE

        override val priority: Byte
            get() = 0
    }

    class PlaceholderProxy(val expansion: PlaceholderExpansion) : me.clip.placeholderapi.expansion.PlaceholderExpansion() {

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
    }
}