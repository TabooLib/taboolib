package taboolib.expansion

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.util.t
import taboolib.common5.*
import java.lang.reflect.Parameter
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.expansion.AnalyzedClass
 *
 * @author 坏黑
 * @since 2023/3/29 11:28
 */
@Suppress("UNCHECKED_CAST")
class AnalyzedClass private constructor(val clazz: Class<*>) {

    /** 主构造器 */
    private val primaryConstructor = clazz.declaredConstructors.firstOrNull { it.parameters.isNotEmpty() } ?: error(
        """
        未找到 $clazz 的主构造器。
        No primary constructor found for $clazz
        """.t()
    )

    /** 成员列表 */
    private val memberProperties = clazz.declaredFields.associateBy { it.name }

    private val mps = memberProperties.entries.toMutableList()

    /** 成员列表 */
    val members = primaryConstructor.parameters.map {
        // 优先按名称匹配，找不到再按类型兜底
        val entry = mps.firstOrNull { e -> e.key == it.name }
            ?: mps.firstOrNull { e -> e.value.type == it.type }
            ?: error(
                """
                在 $clazz 类中，未找到成员 ${it.name}。
                No member found for $it in $clazz
                """.t()
            )
        mps.remove(entry)
        val final = entry.value.modifiers and 16 != 0
        AnalyzedClassMember(validation(it), entry.value.name, final)
    }

    /** 主成员 */
    val primaryMember = members.firstOrNull { it.isPrimary }

    /** 主成员名称 */
    val primaryMemberName = primaryMember?.name

    /** 反序列化所在伴生类实例 */
    val wrapperObjectInstance = runCatching { clazz.getProperty<Any>("Companion", isStatic = true) }.getOrNull()

    /** 反序列化方法 */
    val wrapperFunction = wrapperObjectInstance?.javaClass?.declaredMethods?.firstOrNull {
        it.parameters.size == 1 && BundleMap::class.java.isAssignableFrom(it.parameters[0].type)
    }

    init {
        val customs = members.filter { it.isCustomObject }
        if (customs.isNotEmpty()) {
            customs.forEach {
                if (CustomTypeFactory.getCustomTypeByClass(it.returnType) == null) {
                    error(
                        """
                            在 ${clazz.simpleName} 类中，成员 ${it.name} 的类型 ${it.returnType} 不受支持。
                            Unsupported type ${it.returnType} for ${it.name} in $clazz
                        """.t()
                    )
                }
            }
        }
        if (members.count { it.isPrimary } > 1) {
            error(
                """
                    在 ${clazz.simpleName} 类中，主成员只能有一个，但找到了 ${members.count { it.isPrimary }} 个。
                    The primary member only supports one, but found ${members.count { it.isPrimary }}
                """.t()
            )
        }
        // 获取访问权限
        memberProperties.forEach { it.value.isAccessible = true }
    }

    /** 获取主成员值 */
    fun getPrimaryMemberValue(data: Any): Any? {
        val property = memberProperties[primaryMember?.propertyName.toString()] ?: error(
            """
                主成员 "$primaryMemberName" 在 $clazz 中未找到。
                Primary member "$primaryMemberName" not found in $clazz
            """.t()
        )
        return property.get(data)
    }

    /** 获取成员值 */
    fun getValue(data: Any, member: AnalyzedClassMember): Any? {
        val property = memberProperties[member.propertyName] ?: error(
            """
                成员 "${member.name}" 在 $clazz 中未找到。
                Member "${member.name}" not found in $clazz
            """.t()
        )
        return property.get(data)
    }

    /** 读取数据 */
    fun read(result: ResultSet): Map<String, Any?> {
        val map = hashMapOf<String, Any?>()
        members.forEach { member ->
            val obj: Any? = result.getObject(member.name)
            if (obj == null) {
                map[member.name] = null
            } else {
                map[member.name] = when {
                    member.isBoolean -> obj.cbool
                    member.isByte -> obj.cbyte
                    member.isShort -> obj.cshort
                    member.isInt -> obj.cint
                    member.isLong -> obj.clong
                    member.isFloat -> obj.cfloat
                    member.isDouble -> obj.cdouble
                    member.isChar -> obj.cint.toChar()
                    member.isString -> obj.toString()
                    member.isUUID -> UUID.fromString(obj.toString())
                    member.isEnum -> member.returnType.enumConstants.firstOrNull { (it as Enum<*>).name == obj.toString() }
                        ?: error(
                            """
                            在 $clazz 类中，成员 ${member.name} 的枚举值 "$obj" 不存在。
                            Enum value "$obj" not found for ${member.name} in $clazz
                            """.t()
                        )
                    member.isByteArray -> obj.cByteArray
                    else -> {
                        val customType = CustomTypeFactory.getCustomTypeByClass(member.returnType) ?: error(
                            """
                            在 $clazz 类中，成员 ${member.name} 的类型 ${member.returnType} 不受支持。
                            Unsupported type ${member.returnType} for ${member.name} in $clazz
                            """.t()
                        )
                        customType.deserialize(obj)
                    }
                }
            }
        }
        return map
    }

    /** 创建实例 */
    fun <T> createInstance(map: Map<String, Any?>): T {
        return if (wrapperFunction != null) {
            wrapperFunction.invoke(wrapperObjectInstance, BundleMapImpl(map)) ?: error(
                """
                无法创建 $clazz 实例。
                Failed to create instance for $clazz
                """.t()
            )
        } else {
            val args = members.map { map[it.name] }
            try {
                primaryConstructor.newInstance(*args.toTypedArray())
            } catch (ex: Throwable) {
                error(
                    """
                    无法创建 $clazz 实例。($args, map=$map)
                    Failed to create instance for $clazz. ($args, map=$map)
                    """.t()
                )
            }
        } as T
    }

    /** 验证参数 */
    fun validation(parameter: Parameter): Parameter {
        // 可变参数
        if (parameter.isVarArgs) {
            error(
                """
                无法在 $parameter 上使用可变参数。
                Vararg parameters are not supported for $parameter
                """.t()
            )
        }
        return parameter
    }

    companion object {

        val cached = ConcurrentHashMap<Class<*>, AnalyzedClass>()

        fun of(clazz: Class<*>): AnalyzedClass {
            return cached.computeIfAbsent(clazz) { AnalyzedClass(it) }
        }
    }
}