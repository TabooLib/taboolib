package taboolib.expansion

import java.lang.reflect.Parameter

/**
 * TabooLib
 * taboolib.expansion.AnalyzedClassMember
 *
 * @author 坏黑
 * @since 2023/3/29 11:28
 */
class AnalyzedClassMember(private val root: Parameter, name: String, val isFinal: Boolean) {

    /** 名称 */
    val name = root.findAnnotation<Alias>()?.value ?: name.toColumnName()

    /** 属性名称 */
    val propertyName: String = name

    /** 返回类型 */
    val returnType: Class<*> = root.type

    /** 是否为 ID 键 */
    val isPrimary = root.findAnnotation<Id>() != null

    /** 是否建立索引 */
    val isKey = root.findAnnotation<Key>() != null

    /** 是否建立唯一索引 */
    val isUniqueKey = root.findAnnotation<UniqueKey>() != null

    /** 长度 */
    val length = root.findAnnotation<Length>()?.value ?: 64

    /** 是否为基础类型（Boolean） */
    val isBoolean: Boolean
        get() = returnType == Boolean::class.java || returnType == Boolean::class.javaPrimitiveType

    /** 是否为基础类型（Byte） */
    val isByte: Boolean
        get() = returnType == Byte::class.java || returnType == Byte::class.javaPrimitiveType

    /** 是否为基础类型（Short） */
    val isShort: Boolean
        get() = returnType == Short::class.java || returnType == Short::class.javaPrimitiveType

    /** 是否为基础类型（Int） */
    val isInt: Boolean
        get() = returnType == Int::class.java || returnType == Int::class.javaPrimitiveType

    /** 是否为基础类型（Long） */
    val isLong: Boolean
        get() = returnType == Long::class.java || returnType == Long::class.javaPrimitiveType

    /** 是否为基础类型（Float） */
    val isFloat: Boolean
        get() = returnType == Float::class.java || returnType == Float::class.javaPrimitiveType

    /** 是否为基础类型（Double） */
    val isDouble: Boolean
        get() = returnType == Double::class.java || returnType == Double::class.javaPrimitiveType

    /** 是否为基础类型（Char） */
    val isChar: Boolean
        get() = returnType == Char::class.java || returnType == Char::class.javaPrimitiveType

    /** 是否为字符串 */
    val isString: Boolean
        get() = returnType == String::class.java

    /** 是否为 UUID */
    val isUUID: Boolean
        get() = returnType == java.util.UUID::class.java

    /** 是否为枚举 */
    val isEnum: Boolean
        get() = Enum::class.java.isAssignableFrom(returnType)

    /** 是否为自定义对象 */
    val isCustomObject: Boolean
        get() = !isBoolean && !isByte && !isShort && !isInt && !isLong && !isFloat && !isDouble && !isChar && !isString && !isEnum && !isUUID

    /** 是否可以转换成字符串类型 */
    fun canConvertedString(): Boolean {
        return isString || isEnum || isUUID
    }

    /** 是否可以转化为数字类型 */
    fun canConvertedNumber(): Boolean {
        return canConvertedInteger() || canConvertedDecimal()
    }

    /** 是否可以转化为整数类型 */
    fun canConvertedInteger(): Boolean {
        return isBoolean || isByte || isShort || isInt || isLong || isChar
    }

    /** 是否可以转化为小数类型 */
    fun canConvertedDecimal(): Boolean {
        return isFloat || isDouble
    }

    override fun toString(): String {
        return "$name(${returnType})"
    }

    companion object {

        /** 转换为数据库字段名称 */
        fun String.toColumnName(): String {
            return toCharArray().joinToString("") { if (it.isUpperCase()) "_${it.lowercase()}" else it.toString() }
        }

        /** 获取注解 */
        inline fun <reified T : Annotation> Parameter.findAnnotation(): T? {
            if (isAnnotationPresent(T::class.java)) {
                return getAnnotation(T::class.java)
            }
            return null
        }
    }
}