package taboolib.expansion

import org.tabooproject.reflex.Reflex.Companion.getProperty
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
    private val primaryConstructor = clazz.declaredConstructors.firstOrNull { it.parameters.isNotEmpty() } ?: error("No primary constructor found for $clazz")

    /** 成员列表 */
    private val memberProperties = clazz.declaredFields.associateBy { it.name }

    private val mps = memberProperties.entries.toMutableList()

    /** 成员列表 */
    val members = primaryConstructor.parameters.map {
        val entry = mps.firstOrNull { e -> e.value.type == it.type } ?: error("No member found for $it in $clazz")
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
            // error("The following members are not supported: $customs")
            customs.forEach {
                if (CustomTypeFactory.getCustomTypeByClass(it.returnType) == null) {
                    error("Unsupported type ${it.returnType} for ${it.name} in $clazz")
                }
            }
        }
        if (members.count { it.isPrimary } > 1) {
            error("The primary member only supports one, but found ${members.count { it.isPrimary }}")
        }
        // 获取访问权限
        memberProperties.forEach { it.value.isAccessible = true }
    }

    /** 获取主成员值 */
    fun getPrimaryMemberValue(data: Any): Any {
        val property = memberProperties[primaryMember?.propertyName.toString()] ?: error("Primary member \"$primaryMemberName\" not found in $clazz")
        return property.get(data)!!
    }

    /** 获取成员值 */
    fun getValue(data: Any, member: AnalyzedClassMember): Any {
        val property = memberProperties[member.propertyName] ?: error("Member \"${member.name}\" not found in $clazz")
        return property.get(data)!!
    }

    /** 读取数据 */
    fun read(result: ResultSet): Map<String, Any?> {
        val map = hashMapOf<String, Any?>()
        members.forEach { member ->
            val obj = result.getObject(member.name)
            val wrap = when {
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
                member.isEnum -> member.returnType.enumConstants.first { it.toString() == obj.toString() }
                else -> {
                    val customType = CustomTypeFactory.getCustomTypeByClass(member.returnType) ?: error("Unsupported type ${member.returnType} for ${member.name} in $clazz")
                    customType.deserialize(obj)
                }
            }
            map[member.name] = wrap
        }
        return map
    }

    /** 创建实例 */
    fun <T> createInstance(map: Map<String, Any?>): T {
        return if (wrapperFunction != null) {
            wrapperFunction.invoke(wrapperObjectInstance, BundleMapImpl(map)) ?: error("Failed to create instance for $clazz")
        } else {
            val args = members.map { map[it.name] }
            try {
                primaryConstructor.newInstance(*args.toTypedArray())
            } catch (ex: Throwable) {
                error("Failed to create instance for $clazz ($args), map=$map")
            }
        } as T
    }

    /** 验证参数 */
    fun validation(parameter: Parameter): Parameter {
        // 可变参数
        if (parameter.isVarArgs) {
            error("Vararg parameters are not supported for $parameter")
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