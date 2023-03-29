package taboolib.expansion

import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common5.*
import java.sql.ResultSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType

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
    private val primaryConstructor = clazz.kotlin.primaryConstructor ?: error("No primary constructor found for $clazz")

    /** 成员列表 */
    private val memberProperties = clazz.kotlin.memberProperties.associateBy { it.name }

    /** 成员列表 */
    val members = primaryConstructor.valueParameters.map {
        // 不知道为什么，isFinal 方法在这里会失效，全部都是 true
        // 测试日志：
        // name=username, p=val com.github.username.GameHistory.username: java.util.UUID, f=true
        // name=game, p=val com.github.username.GameHistory.game: kotlin.String, f=true
        // name=playTimes, p=var com.github.username.GameHistory.playTimes: kotlin.Int, f=true
        // name=deepLevel, p=var com.github.username.GameHistory.deepLevel: kotlin.Int, f=true
        // name=useTime, p=var com.github.username.GameHistory.useTime: kotlin.Long, f=true
        AnalyzedClassMember(validation(it), memberProperties[it.name]?.toString()?.startsWith("var") != true)
    }

    /** 主成员 */
    val primaryMember = members.firstOrNull { it.isPrimary }

    /** 主成员名称 */
    val primaryMemberName = primaryMember?.name

    /** 反序列化所在伴生类实例 */
    val wrapperObjectInstance = clazz.kotlin.companionObjectInstance

    /** 反序列化方法 */
    val wrapperFunction = clazz.kotlin.companionObject?.functions?.firstOrNull {
        it.valueParameters.size == 1 && BundleMap::class.java.isAssignableFrom(it.valueParameters[0].type.javaType as Class<*>)
    }

    init {
        val customs = members.filter { it.isCustomObject }
        if (customs.isNotEmpty()) {
            error("The following members are not supported: $customs")
        }
        if (members.count { it.isPrimary } > 1) {
            error("The primary member only supports one, but found ${members.count { it.isPrimary }}")
        }
    }

    /** 获取主成员值 */
    fun getPrimaryMemberValue(data: Any): Any {
        val property = memberProperties[primaryMember?.propertyName] ?: error("Primary member \"$primaryMemberName\" not found in $clazz")
        return property.call(data)!!
    }

    /** 获取成员值 */
    fun getValue(data: Any, member: AnalyzedClassMember): Any {
        val property = memberProperties[member.propertyName] ?: error("Member \"${member.name}\" not found in $clazz")
        return property.call(data)!!
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
                else -> error("Unsupported type ${member.returnType} for ${member.name} in $clazz")
            }
            map[member.name] = wrap
        }
        return map
    }

    /** 创建实例 */
    fun <T> createInstance(map: Map<String, Any?>): T {
        return if (wrapperFunction != null) {
            wrapperFunction.call(wrapperObjectInstance, BundleMapImpl(map)) ?: error("Failed to create instance for $clazz")
        } else {
            val args = members.map { map[it.name] }
            try {
                primaryConstructor.call(*args.toTypedArray())
            } catch (ex: Throwable) {
                error("Failed to create instance for $clazz ($args), map=$map")
            }
        } as T
    }

    /** 验证参数 */
    fun validation(parameter: KParameter): KParameter {
        if (parameter.name == null) {
            error("Parameter name is null for $parameter")
        }
        if (parameter.isVararg) {
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