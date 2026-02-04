package taboolib.module.nms.remap

/**
 * 动态 NMS 调用操作码
 *
 * 在字节码转换阶段，dynamic() 调用会被替换为对应的 JVM 指令，
 * 运行时与手写 NMS 调用完全等价，零反射开销。
 *
 * - [INVOKEVIRTUAL] — 调用实例方法（含 abstract / interface 方法）。
 *   JVM 会在运行时根据对象实际类型进行虚分派，因此无论目标方法声明在
 *   class、abstract class 还是 interface 上，一律使用此操作码即可。
 *   不需要区分 INVOKEINTERFACE，transformer 会根据目标类型自动处理。
 * - [INVOKESTATIC] — 调用静态方法。包括 companion object 中的 @JvmStatic 方法。
 * - [INVOKESPECIAL] — 调用构造函数（`<init>`）。会生成 NEW + DUP + INVOKESPECIAL 序列，
 *   返回新创建的实例。不用于调用 super 或 private 方法。
 * - [GETFIELD] — 读取实例字段，args[0] 为实例对象。
 * - [PUTFIELD] — 写入实例字段，args[0] 为实例对象，args[1] 为新值。返回 null。
 * - [GETSTATIC] — 读取静态字段，无需传入 args。
 * - [PUTSTATIC] — 写入静态字段，args[0] 为新值。返回 null。
 *
 * @author sky
 */
@Suppress("SpellCheckingInspection")
enum class DynamicOpcode {
    INVOKEVIRTUAL,
    INVOKESTATIC,
    INVOKESPECIAL,
    GETFIELD,
    PUTFIELD,
    GETSTATIC,
    PUTSTATIC
}

/**
 * 动态 NMS 调用标记函数
 *
 * 该函数本身不会被执行，在字节码转换阶段会被替换为直接 JVM 指令。
 *
 * **重要约束：descriptor 参数必须是编译期常量（字面量或 const val）。**
 * 字符串模板（`"$var#method"`）会被编译为运行时拼接，transformer 无法识别。
 *
 * 描述符格式：
 * - 方法：`类名#方法名(参数描述符)返回描述符`
 * - 构造：`类名(参数描述符)V`
 * - 字段：`类名#字段名:字段描述符`
 *
 * 类名使用点号分隔，描述符中的类引用支持点号或斜杠。
 * 字段描述符支持简写类名（自动补 `L...;`）。
 * 可使用 `{version}` 占位符代替 CraftBukkit 版本号。
 *
 * ```kotlin
 * // 读取静态字段
 * val registry = dynamic(GETSTATIC, "net.minecraft.core.registries.BuiltInRegistries#ENTITY_TYPE:net.minecraft.core.RegistryBlocks")
 * // 调用实例方法（interface / abstract 自动处理）
 * val id = dynamic(INVOKEVIRTUAL, "net.minecraft.core.IRegistry#getId(java.lang.Object;)I", registry, key) as Int
 * // 读取实例字段
 * val conn = dynamic(GETFIELD, "net.minecraft.server.v1_16_R3.EntityPlayer#connection:net.minecraft.server.v1_16_R3.PlayerConnection", player)
 * // 调用静态方法
 * val component = dynamic(INVOKESTATIC, "net.minecraft.network.chat.IChatBaseComponent.ChatSerializer#fromJson(java.lang.String;)net.minecraft.network.chat.IChatBaseComponent;", json)
 * // 构造新实例
 * val packet = dynamic(INVOKESPECIAL, "net.minecraft.server.v1_16_R3.PacketPlayOutChat(net.minecraft.network.chat.IChatBaseComponent;B)V", component, 1.toByte())
 * // 使用 {version} 占位符
 * val handle = dynamic(GETFIELD, "org.bukkit.craftbukkit.{version}.entity.CraftPlayer#handle:net.minecraft.server.v1_16_R3.EntityPlayer", player)
 * ```
 *
 * @author sky
 */
@Suppress("UNUSED_PARAMETER")
fun dynamic(opcode: DynamicOpcode, descriptor: String, vararg args: Any?): Any? {
    error("dynamic() 不应在运行时被调用，字节码转换未生效")
}
