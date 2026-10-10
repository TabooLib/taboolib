package taboolib.platform.type

import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

class BukkitPlayerTest {

    @Test
    fun `null bed spawn location reaches bukkit setter`() {
        var calls = 0
        val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { _, method, args ->
            if (method.name == "setBedSpawnLocation" && method.parameterCount == 1) {
                calls++
                assertNull(args?.firstOrNull())
                null
            } else {
                defaultValue(method.returnType)
            }
        } as Player

        BukkitPlayer(player).bedSpawnLocation = null

        assertEquals(1, calls)
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
