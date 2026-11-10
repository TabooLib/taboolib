package taboolib.platform

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.platform.type.VelocityCommandSender
import taboolib.platform.type.VelocityPlayer
import java.lang.reflect.Proxy

class VelocityAdapterTest {

    private val adapter = VelocityAdapter()

    @Test
    fun `existing proxy player keeps identity in both adapter paths`() {
        val player = proxy<ProxyPlayer>()

        assertSame(player, adapter.adaptPlayer(player))
        assertSame(player, adapter.adaptCommandSender(player))
    }

    @Test
    fun `existing proxy command sender keeps identity`() {
        val sender = proxy<ProxyCommandSender>()

        assertSame(sender, adapter.adaptCommandSender(sender))
    }

    @Test
    fun `velocity player remains a proxy player through sender adapter`() {
        val player = proxy<Player>()

        val adaptedPlayer = adapter.adaptPlayer(player)
        val adaptedSender = adapter.adaptCommandSender(player)

        assertTrue(adaptedPlayer is VelocityPlayer)
        assertTrue(adaptedSender is VelocityPlayer)
        assertSame(player, adaptedPlayer.origin)
        assertSame(player, adaptedSender.origin)
    }

    @Test
    fun `non-player command source uses command sender adapter`() {
        val sender = proxy<CommandSource>()

        val adapted = adapter.adaptCommandSender(sender)

        assertTrue(adapted is VelocityCommandSender)
        assertSame(sender, adapted.origin)
    }

    private inline fun <reified T> proxy(): T {
        return Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { instance, method, args ->
            when (method.name) {
                "equals" -> instance === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(instance)
                "toString" -> "${T::class.java.simpleName}Proxy"
                else -> defaultValue(method.returnType)
            }
        } as T
    }

    private fun defaultValue(type: Class<*>): Any? {
        return when (type) {
            java.lang.Boolean.TYPE -> false
            java.lang.Byte.TYPE -> 0.toByte()
            java.lang.Short.TYPE -> 0.toShort()
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0F
            java.lang.Double.TYPE -> 0.0
            java.lang.Character.TYPE -> '\u0000'
            else -> null
        }
    }
}
