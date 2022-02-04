package taboolib.common.platform.service

/**
 * TabooLib
 * taboolib.common.platform.service.PlatformRunnable
 *
 * @author sky
 * @since 2021/6/15 11:17 下午
 */
class PlatformRunnable(val async: Boolean, val delay: Long, val period: Long, val commit: String?, val executor: PlatformTask.() -> Unit)