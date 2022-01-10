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
 */
fun info(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().info(*message)
}

/**
 * 打印错误日志
 */
fun severe(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().severe(*message)
}

/**
 * 打印警告日志
 */
fun warning(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().warning(*message)
}

/**
 * 释放当前插件内的特定资源文件
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

/**
 *  @param folder 插件资源主路径
 * @param child 资源路径
 * @param message 文件不存在时输出
 * @param mkdirs 是否创建示例配置
 * @param example 示例配置名称
 * @return 返回资源路径的所有文件
 */
fun getFiles(folder: String, child: String, message: String, mkdirs: Boolean, example: String = "example.yml"): File {
    val file = File(folder, child)
    if (!file.exists() && mkdirs) { // 如果 <child> 不存在时释放示例配置
        if (message.isNotEmpty()) PlatformFactory.getService<PlatformIO>().info(message)
        PlatformFactory.getService<PlatformIO>().releaseResourceFile("$child/$example", true)
    }
    return file
}