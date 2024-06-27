package taboolib.common.platform.function

import taboolib.common.PrimitiveIO
import taboolib.common.io.isDevelopmentMode
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformIO
import java.io.File

/**
 * 获取控制台对象
 * 例如：
 * server<ConsoleCommandSender>()
 */
fun <T> server(): T {
    return PlatformFactory.getService<PlatformIO>().server()
}

/**
 * 打印开发者日志
 *
 * @param message 日志内容
 */
fun dev(vararg message: Any?) {
    if (isDevelopmentMode) {
        message.filterNotNull().forEach { PrimitiveIO.dev(it) }
    }
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
 * @param source 资源文件源路径
 * @param replace 是否覆盖文件
 * @param target 资源文件目标路径
 */
fun releaseResourceFile(source: String, replace: Boolean = false, target: String = source): File {
    return PlatformFactory.getService<PlatformIO>().releaseResourceFile(source, target, replace)
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