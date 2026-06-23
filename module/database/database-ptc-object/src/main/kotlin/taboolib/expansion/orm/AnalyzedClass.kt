package taboolib.expansion.orm

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.util.t
import taboolib.expansion.BundleMap
import taboolib.expansion.CustomTypeFactory
import java.lang.reflect.Parameter
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

    /** 主构造器（可能为 null，表示使用字段扫描模式） */
    internal val primaryConstructor = clazz.declaredConstructors.firstOrNull { it.parameters.isNotEmpty() }

    /** 是否使用字段扫描模式（无带参构造器） */
    internal val fieldScanMode = primaryConstructor == null

    /** 无参构造器（字段扫描模式使用） */
    internal val noArgConstructor = if (fieldScanMode) {
        (clazz.declaredConstructors.firstOrNull { it.parameters.isEmpty() }
            ?: error(
                """
                未找到 $clazz 的构造器。
                No constructor found for $clazz
                """.t()
            )).also { it.isAccessible = true }
    } else null

    /** 成员列表（包含继承字段） */
    internal val memberProperties = collectAllFields(clazz)

    private val mps = memberProperties.entries.toMutableList()

    /** 成员列表 */
    val members = if (fieldScanMode) {
        // 字段扫描模式：从所有非 static、非 synthetic 字段构建
        memberProperties.values.map { field ->
            val isFinal = field.modifiers and java.lang.reflect.Modifier.FINAL != 0
            AnalyzedClassMember.fromField(field, field.name, isFinal)
        }
    } else {
        primaryConstructor!!.parameters.map {
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
            AnalyzedClassMember.fromParameter(validation(it), entry.value.name, final)
        }
    }

    /** 主成员 */
    val primaryMember = members.firstOrNull { it.isPrimary }

    /** 主成员名称 */
    val primaryMemberName = primaryMember?.name

    /** 实际映射到列的成员（排除 @Ignore、@LinkTable 成员和容器成员） */
    val columnMembers = members.filter { !it.isIgnored && !it.isLinkTable && !it.isCollection }

    /** @LinkTable 成员列表 */
    val linkMembers = members.filter { it.isLinkTable }

    /** 是否存在 @LinkTable 成员 */
    val hasLinkMembers = linkMembers.isNotEmpty()

    /** 是否存在 @Ignore 成员 */
    val hasIgnoredMembers = members.any { it.isIgnored }

    /**
     * Kotlin 合成默认构造器（带 DefaultConstructorMarker 参数）。
     * 仅在存在 @Ignore 成员时查找，用于利用 Kotlin 声明的默认值。
     */
    internal val kotlinDefaultConstructor = if (hasIgnoredMembers && !fieldScanMode) {
        clazz.declaredConstructors.firstOrNull { ctor ->
            val params = ctor.parameterTypes
            params.size >= 2
                && params[params.size - 1].name == "kotlin.jvm.internal.DefaultConstructorMarker"
                && params[params.size - 2] == Int::class.javaPrimitiveType
        }?.also { it.isAccessible = true }
    } else null

    /** 容器类型成员列表（List/Set/Map） */
    val collectionMembers = members.filter { it.isCollection }

    /** 是否存在容器类型成员 */
    val hasCollectionMembers = collectionMembers.isNotEmpty()

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
        // 验证 @LinkTable 成员的关联类必须有 @Id 字段
        linkMembers.forEach { member ->
            val linkClass = member.linkTableClass!!
            val linkedClass = AnalyzedClass.of(linkClass)
            if (linkedClass.primaryMember == null) {
                error(
                    """
                        在 ${clazz.simpleName} 类中，@LinkTable 成员 ${member.propertyName} 的关联类 ${linkClass.simpleName} 没有 @Id 字段。
                        Linked class ${linkClass.simpleName} for @LinkTable member ${member.propertyName} in ${clazz.simpleName} has no @Id field.
                    """.t()
                )
            }
        }
        // 验证容器类型成员
        collectionMembers.forEach { member ->
            require(member.parameterizedType != null) {
                """
                    在 ${clazz.simpleName} 类中，容器类型成员 ${member.propertyName} 缺少泛型信息。
                    Collection member ${member.propertyName} in ${clazz.simpleName} is missing generic type information.
                """.t()
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
            cached[clazz]?.let { return it }
            val instance = AnalyzedClass(clazz)
            return cached.putIfAbsent(clazz, instance) ?: instance
        }

        /** 收集类及其父类的所有实例字段（排除 static/synthetic） */
        private fun collectAllFields(clazz: Class<*>): Map<String, java.lang.reflect.Field> {
            val fields = LinkedHashMap<String, java.lang.reflect.Field>()
            var current: Class<*>? = clazz
            while (current != null && current != Any::class.java) {
                for (field in current.declaredFields) {
                    if (java.lang.reflect.Modifier.isStatic(field.modifiers)) continue
                    if (field.isSynthetic) continue
                    fields.putIfAbsent(field.name, field)
                }
                current = current.superclass
            }
            return fields
        }
    }
}
