package taboolib.common.platform

import taboolib.common.io.Isolated

/**
 * TabooLib
 * taboolib.common.platform.EventPriority
 *
 * @author sky
 * @since 2021/6/16 1:07 上午
 */
@Isolated([SubscribeEvent::class])
enum class EventPriority(val level: Int) {

    LOWEST(-64), LOW(-32), NORMAL(0), HIGH(32), HIGHEST(64), MONITOR(128)
}