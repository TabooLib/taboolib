package taboolib.common.platform.service

import taboolib.common.platform.PlatformService

/**
 * TabooLib
 * taboolib.common.platform.service.PlatformExecutor
 *
 * @author sky
 * @since 2021/6/15 11:17 下午
 */
@PlatformService
interface PlatformExecutor {

    fun start()

    fun submit(runnable: PlatformRunnable): PlatformTask
}