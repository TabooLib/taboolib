package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformIO
import java.io.File

/**
 * 获取当前插件名称
 */
val pluginId: String
    get() = PlatformFactory.getService<PlatformIO>().pluginId

/**
 * 获取当前插件版本
 */
val pluginVersion: String
    get() = PlatformFactory.getService<PlatformIO>().pluginVersion

/**
 * 当前是否在主线程中运行
 */
val isPrimaryThread: Boolean
    get() = PlatformFactory.getService<PlatformIO>().isPrimaryThread

/**
 * 获取控制台对象
 * 例如：
 * server<ConsoleCommandSender>()
 */
fun <T> server(): T {
    return PlatformFactory.getService<PlatformIO>().server()
}

/**
 * 打印日志
 *
 * @param message 日志内容
 */
fun info(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().info(*message)
}

/**
 * 打印错误日志
 *
 * @param message 日志内容
 */
fun severe(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().severe(*message)
}

/**
 * 打印警告日志
 *
 * @param message 日志内容
 */
fun warning(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().warning(*message)
}

/**
 * 释放当前插件内的特定资源文件
 *
 * @param path 资源文件路径
 * @param replace 是否覆盖文件
 */
fun releaseResourceFile(path: String, replace: Boolean = false): File {
    return PlatformFactory.getService<PlatformIO>().releaseResourceFile(path, replace)
}

/**
 * 获取当前插件的 Jar 文件对象
 */
fun getJarFile(): File {
    return PlatformFactory.getService<PlatformIO>().getJarFile()
}

/**
 * 获取当前插件的配置文件目录
 * 可能不存在，需要手动调用 mkdirs 方法创建
 */
fun getDataFolder(): File {
    return PlatformFactory.getService<PlatformIO>().getDataFolder()
}

/**
 * 获取当前平台的信息
 * 用于 BStats 统计，无实际用途
 */
fun getPlatformData(): Map<String, Any> {
    return PlatformFactory.getService<PlatformIO>().getPlatformData()
}