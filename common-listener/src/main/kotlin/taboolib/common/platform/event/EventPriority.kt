package taboolib.common.platform.event

/**
 * @author sky
 * @since 2021/6/16 1:07 上午
 */
@Suppress("MagicNumber")
enum class EventPriority(val level: Int) {

    LOWEST(-64), LOW(-32), NORMAL(0), HIGH(32), HIGHEST(64), MONITOR(128)
}
