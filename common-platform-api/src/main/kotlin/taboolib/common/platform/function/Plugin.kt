@file:Isolated

package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformIO

/**
 * 获取当前插件名称
 */
inline val pluginId: String
    get() = PlatformFactory.getService<PlatformIO>().pluginId

/**
 * 获取当前插件版本
 */
inline val pluginVersion: String
    get() = PlatformFactory.getService<PlatformIO>().pluginVersion

/**
 * 当前是否在主线程中运行
 */
inline val isPrimaryThread: Boolean
    get() = PlatformFactory.getService<PlatformIO>().isPrimaryThread