package taboolib.common.platform

import java.util.*

/**
 * TabooLib
 * taboolib.common.platform.ProxyGameMode
 *
 * @author sky
 * @since 2021/7/4 10:21 下午
 */
enum class ProxyGameMode {

    CREATIVE, SURVIVAL, ADVENTURE, SPECTATOR;

    companion object {

        fun fromString(value: String) = when (value.uppercase(Locale.getDefault())) {
            "SURVIVAL", "0" -> SURVIVAL
            "CREATIVE", "1" -> CREATIVE
            "ADVENTURE", "2" -> ADVENTURE
            "SPECTATOR", "3" -> SPECTATOR
            else -> error("Unknown GameMode $value")
        }
    }
}
