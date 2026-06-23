package taboolib.expansion.orm

import taboolib.common.reflect.getAnnotationIfPresent
import taboolib.expansion.*
import taboolib.module.database.ColumnTypePostgreSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.ColumnTypeSQLite
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType

/**
 * 包装 Field，同时检查 Kotlin property 注解。
 *
 * Kotlin 对 body property 上的注解，如果注解同时支持 FIELD 和 PROPERTY 目标，
 * 可能会被放到 property 目标上（而非 field），此时 Java 的 Field.getAnnotation() 无法读取。
 * 本类通过检查 Kotlin 编译器生成的 `<name>$annotations` 合成方法来兜底获取 property 注解。
 */
private class FieldWithPropertyAnnotations(private val field: Field) : AnnotatedElement {

    /** 从 Kotlin 生成的 $annotations 合成方法中获取 property 注解 */
    private val propertyAnnotations: Array<Annotation> by lazy {
        try {
            // Kotlin 生成的合成方法名格式: get<PropertyName>$annotations
            val getterName = "get${field.name.replaceFirstChar { it.uppercase() }}\$annotations"
            val method = field.declaringClass.getDeclaredMethod(getterName)
            method.isAccessible = true
            method.annotations
        } catch (_: Throwable) {
            emptyArray()
        }
    }

    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        field.getAnnotation(annotationClass)?.let { return it }
        @Suppress("UNCHECKED_CAST")
        return propertyAnnotations.firstOrNull { annotationClass.isInstance(it) } as? T
    }

    override fun getAnnotations(): Array<Annotation> {
        return field.annotations + propertyAnnotations
    }

    override fun getDeclaredAnnotations(): Array<Annotation> {
        return field.declaredAnnotations + propertyAnnotations
    }

    override fun isAnnotationPresent(annotationClass: Class<out Annotation>): Boolean {
        return field.isAnnotationPresent(annotationClass) || propertyAnnotations.any { annotationClass.isInstance(it) }
    }
}

/**
 * TabooLib
 * taboolib.expansion.AnalyzedClassMember
 *
 * @author 坏黑
 * @since 2023/3/29 11:28
 */
class AnalyzedClassMember(private val root: AnnotatedElement, name: String, val isFinal: Boolean, val returnType: Class<*>, genericType: java.lang.reflect.Type?) {

    /** 名称 */
    val name = root.findAnnotation<Alias>()?.value ?: name.toColumnName()

    /** 属性名称 */
    val propertyName: String = name

    /** 泛型参数化类型 */
    val parameterizedType: ParameterizedType? = genericType as? ParameterizedType

    /** 是否为忽略字段（优先级最高，不参与数据库读写） */
    val isIgnored: Boolean = root.findAnnotation<Ignore>() != null

    /** 是否为 ID 键（@Ignore 优先级最高，忽略字段不作为主键） */
    val isPrimary = !isIgnored && root.findAnnotation<Id>() != null

    /** 是否建立索引（@Ignore 优先级最高） */
    val isKey = !isIgnored && root.findAnnotation<Key>() != null

    /** 是否建立唯一索引（@Ignore 优先级最高） */
    val isUniqueKey = !isIgnored && root.findAnnotation<UniqueKey>() != null

    /** 是否不为空（@Ignore 优先级最高） */
    val isNotNull = !isIgnored && root.findAnnotation<NotNull>() != null

    /** 长度 */
    val length = root.findAnnotation<Length>()?.value ?: 64

    /** 自定义 SQL 列类型 */
    val columnTypeSQL: ColumnTypeSQL? = root.findAnnotation<ColumnType>()?.sql

    /** 自定义 SQLite 列类型 */
    val columnTypeSQLite: ColumnTypeSQLite? = root.findAnnotation<ColumnType>()?.sqlite

    /** 自定义 PostgreSQL 列类型 */
    val columnTypePostgreSQL: ColumnTypePostgreSQL? = root.findAnnotation<ColumnType>()?.postgresql

    /** 是否指定了自定义列类型 */
    val hasColumnType: Boolean = root.findAnnotation<ColumnType>() != null

    /** 是否为关联表引用 */
    val isLinkTable: Boolean = !isIgnored && root.findAnnotation<LinkTable>() != null

    /** 关联表外键列名（下划线命名） */
    val linkTableColumn: String? = root.findAnnotation<LinkTable>()?.value?.toColumnName()

    /** 关联的 data class 类型 */
    val linkTableClass: Class<*>? = if (isLinkTable) returnType else null

    /** 是否为 List 类型 */
    val isList: Boolean
        get() = List::class.java.isAssignableFrom(returnType)

    /** 是否为 Set 类型 */
    val isSet: Boolean
        get() = Set::class.java.isAssignableFrom(returnType)

    /** 是否为 Map 类型 */
    val isMap: Boolean
        get() = Map::class.java.isAssignableFrom(returnType)

    /** 是否为扁平化集合（整个集合作为单列存储，由集合 CustomType 序列化） */
    val isFlattenedCollection: Boolean
        get() = !isIgnored && (isList || isSet || isMap)
                && collectionElementType != null
                && CustomTypeFactory.getCustomTypeForCollection(returnType, collectionElementType!!) != null

    /** 是否为容器类型（List/Set/Map），@Ignore 字段和扁平化集合不视为容器成员 */
    val isCollection: Boolean
        get() = !isIgnored && (isList || isSet || isMap) && !isFlattenedCollection

    /** 容器元素类型（List<T>/Set<T> 的 T，Map<K,V> 的 V） */
    val collectionElementType: Class<*>? = when {
        parameterizedType != null && (List::class.java.isAssignableFrom(returnType) || Set::class.java.isAssignableFrom(returnType)) -> {
            val arg = parameterizedType.actualTypeArguments.firstOrNull()
            arg as? Class<*> ?: if (arg is ParameterizedType) arg.rawType as? Class<*> else null
        }
        parameterizedType != null && Map::class.java.isAssignableFrom(returnType) -> {
            val args = parameterizedType.actualTypeArguments
            val arg = args.getOrNull(1)
            arg as? Class<*> ?: if (arg is ParameterizedType) arg.rawType as? Class<*> else null
        }
        else -> null
    }

    /** Map 的 Key 类型 */
    val mapKeyType: Class<*>? = if (parameterizedType != null && Map::class.java.isAssignableFrom(returnType)) {
        val arg = parameterizedType.actualTypeArguments.firstOrNull()
        arg as? Class<*> ?: if (arg is ParameterizedType) arg.rawType as? Class<*> else null
    } else null

    /** 是否为基础类型（Boolean） */
    val isBoolean: Boolean
        get() = returnType == Boolean::class.java || returnType == Boolean::class.javaPrimitiveType

    /** 是否为基础类型（Byte） */
    val isByte: Boolean
        get() = returnType == Byte::class.java || returnType == Byte::class.javaPrimitiveType

    /** 是否为基础类型（Short） */
    val isShort: Boolean
        get() = returnType == Short::class.java || returnType == Short::class.javaPrimitiveType

    /** 是否为基础类型（ByteArray） */
    val isByteArray: Boolean
        get() = returnType == ByteArray::class.java || returnType == ByteArray::class.javaPrimitiveType

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

    /** 是否为实现了 [IndexedEnum] 接口的枚举（数据库中以数值存储） */
    val isIndexedEnum: Boolean
        get() = isEnum && IndexedEnum::class.java.isAssignableFrom(returnType)

    /** 是否为自定义对象（@Ignore 字段、扁平化集合不视为自定义对象） */
    val isCustomObject: Boolean
        get() = !isIgnored && !isLinkTable && !isCollection && !isFlattenedCollection && !isBoolean && !isByte && !isShort && !isInt && !isLong && !isFloat && !isDouble && !isChar && !isString && !isEnum && !isUUID && !isByteArray

    /** 是否可以转换成字符串类型 */
    fun canConvertedString(): Boolean {
        return isString || (isEnum && !isIndexedEnum) || isUUID
    }

    /** 是否可以转化为数字类型 */
    fun canConvertedNumber(): Boolean {
        return canConvertedInteger() || canConvertedDecimal()
    }

    /** 是否可以转化为整数类型 */
    fun canConvertedInteger(): Boolean {
        return isBoolean || isByte || isShort || isInt || isLong || isChar || isIndexedEnum
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
            return toCharArray().joinToString("") { if (it.isUpperCase()) "_${it.lowercase()}" else it.toString() }.trimStart('_')
        }

        /** 解析数据类的表名：优先使用 @TableName 注解值，否则将类名转为下划线命名；支持 schema 前缀 */
        fun Class<*>.resolveTableName(): String {
            val annotation = getAnnotation(TableName::class.java)
            val name = annotation?.value ?: simpleName.toColumnName()
            val schema = annotation?.schema ?: ""
            return if (schema.isNotEmpty()) "$schema.$name" else name
        }

        /** 获取注解 */
        inline fun <reified T : Annotation> AnnotatedElement.findAnnotation(): T? {
            return getAnnotationIfPresent(T::class.java)
        }

        /** 从构造器参数创建 */
        fun fromParameter(param: Parameter, name: String, isFinal: Boolean): AnalyzedClassMember {
            return AnalyzedClassMember(param, name, isFinal, param.type, param.parameterizedType)
        }

        /** 从字段创建（同时检查 Kotlin property 注解） */
        fun fromField(field: Field, name: String, isFinal: Boolean): AnalyzedClassMember {
            return AnalyzedClassMember(FieldWithPropertyAnnotations(field), name, isFinal, field.type, field.genericType)
        }

        /** 判断是否为基础类型（可直接映射到数据库列的类型） */
        fun isPrimitiveOrBasicType(clazz: Class<*>): Boolean {
            return clazz == Boolean::class.java || clazz == Boolean::class.javaPrimitiveType
                || clazz == Byte::class.java || clazz == Byte::class.javaPrimitiveType
                || clazz == Short::class.java || clazz == Short::class.javaPrimitiveType
                || clazz == Int::class.java || clazz == Int::class.javaPrimitiveType
                || clazz == Long::class.java || clazz == Long::class.javaPrimitiveType
                || clazz == Float::class.java || clazz == Float::class.javaPrimitiveType
                || clazz == Double::class.java || clazz == Double::class.javaPrimitiveType
                || clazz == Char::class.java || clazz == Char::class.javaPrimitiveType
                || clazz == String::class.java
                || clazz == java.util.UUID::class.java
                || Enum::class.java.isAssignableFrom(clazz)
                || clazz == ByteArray::class.java
        }
    }
}
