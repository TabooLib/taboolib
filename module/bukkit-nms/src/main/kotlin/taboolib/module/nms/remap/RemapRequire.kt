package taboolib.module.nms.remap

/**
 * 运行时类存在性检查标记函数
 * 在字节码转换阶段会被替换为实际的布尔值
 *
 * 使用示例:
 * ```kotlin
 * if (require(net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer::class.java)) {
 *     // 类存在，使用该版本的实现
 * } else {
 *     // 类不存在，使用备用实现
 * }
 * ```
 *
 * @param clazz 需要检查的类
 * @return 始终返回 true（实际值会在字节码转换时确定）
 */
@Suppress("UNUSED_PARAMETER")
fun require(clazz: Class<*>): Boolean {
    // 这是一个标记函数，仅作为编译时的占位符
    // 实际的逻辑会在字节码转换阶段被替换为 true 或 false
    return true
}