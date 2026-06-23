package taboolib.expansion.operator

import taboolib.expansion.*
import taboolib.expansion.orm.AnalyzedClass
import taboolib.expansion.orm.AnalyzedClassMember
import taboolib.module.database.ActionSelect
import taboolib.module.database.Filter
import taboolib.module.database.asFormattedColumnName
import java.sql.Connection

/**
 * Collection 子表处理器（Map/List/Set 存储）
 *
 * 封装容器类型成员的子表 CRUD 和 Accessor 工厂方法。
 */
internal class CollectionTableHandler(
    private val collectionTableInfos: List<CollectionTableInfo>,
    private val executor: DataExecutor
) {

    // === Accessor 工厂方法 ===

    /**
     * 通过父记录 @Id 值获取 Map 类型字段的数据库代理
     */
    fun mapAccessor(id: Any, fieldName: String, sharedConnection: Connection?): DatabaseMap {
        val info = resolveCollectionTableInfo(fieldName) { it.isMap }
        return DatabaseMap(executor.dataSource, info, id.value(), sharedConnection)
    }

    /**
     * 通过 Filter 查找父记录，返回 Map 类型字段的数据库代理
     */
    fun mapAccessor(fieldName: String, filter: Filter.() -> Unit, sharedConnection: Connection?): DatabaseMap {
        val info = resolveCollectionTableInfo(fieldName) { it.isMap }
        val parentId = resolveParentId(filter)
        return DatabaseMap(executor.dataSource, info, parentId, sharedConnection)
    }

    /**
     * 通过父记录 @Id 值获取 List 类型字段的数据库代理
     */
    fun listAccessor(id: Any, fieldName: String, sharedConnection: Connection?): DatabaseList {
        val info = resolveCollectionTableInfo(fieldName) { it.isList }
        return DatabaseList(executor.dataSource, info, id.value(), sharedConnection)
    }

    /**
     * 通过 Filter 查找父记录，返回 List 类型字段的数据库代理
     */
    fun listAccessor(fieldName: String, filter: Filter.() -> Unit, sharedConnection: Connection?): DatabaseList {
        val info = resolveCollectionTableInfo(fieldName) { it.isList }
        val parentId = resolveParentId(filter)
        return DatabaseList(executor.dataSource, info, parentId, sharedConnection)
    }

    /**
     * 通过父记录 @Id 值获取 Set 类型字段的数据库代理
     */
    fun setAccessor(id: Any, fieldName: String, sharedConnection: Connection?): DatabaseSet {
        val info = resolveCollectionTableInfo(fieldName) { it.isSet }
        return DatabaseSet(executor.dataSource, info, id.value(), sharedConnection)
    }

    /**
     * 通过 Filter 查找父记录，返回 Set 类型字段的数据库代理
     */
    fun setAccessor(fieldName: String, filter: Filter.() -> Unit, sharedConnection: Connection?): DatabaseSet {
        val info = resolveCollectionTableInfo(fieldName) { it.isSet }
        val parentId = resolveParentId(filter)
        return DatabaseSet(executor.dataSource, info, parentId, sharedConnection)
    }

    // === 数据操作 ===

    /**
     * 插入容器数据（批量）
     */
    fun insertCollectionData(typeClass: AnalyzedClass, dataList: List<Any>) {
        if (!typeClass.hasCollectionMembers) return
        dataList.forEach { data -> syncCollectionData(typeClass, data) }
    }

    /**
     * 同步单条记录的容器数据（先删后插）
     */
    fun syncCollectionData(typeClass: AnalyzedClass, data: Any) {
        if (!typeClass.hasCollectionMembers) return
        val parentId = typeClass.getPrimaryMemberValue(data)?.value() ?: return
        typeClass.collectionMembers.forEach { member ->
            val info = findCollectionTableInfo(member) ?: return@forEach
            val collection = typeClass.getValue(data, member)
            deleteCollectionByFk(info, parentId)
            if (collection != null) {
                insertCollectionValues(info, member, parentId, collection)
            }
        }
    }

    /**
     * 按 FK 值批量删除容器子表数据
     */
    fun deleteCollectionDataByFk(parentId: Any) {
        collectionTableInfos.forEach { info: CollectionTableInfo ->
            deleteCollectionByFk(info, parentId.value())
        }
    }

    /**
     * 批量加载容器数据并注入到结果 map 中
     */
    fun loadCollections(typeClass: AnalyzedClass, results: List<MutableMap<String, Any?>>) {
        if (!typeClass.hasCollectionMembers) return
        val primaryName = typeClass.primaryMemberName ?: return
        val ids = results.mapNotNull { it[primaryName] }
        if (ids.isEmpty()) return
        executor.setupQuoter()
        typeClass.collectionMembers.forEach { member ->
            val info = findCollectionTableInfo(member) ?: return@forEach
            val collectionData = batchLoadCollection(info, member, ids)
            results.forEach { map ->
                val id = map[primaryName]
                map[member.name] = collectionData[id?.toString()] ?: emptyCollection(member)
            }
        }
    }

    // === 私有辅助方法 ===

    /**
     * 查找容器成员对应的 CollectionTableInfo
     */
    private fun findCollectionTableInfo(member: AnalyzedClassMember): CollectionTableInfo? {
        return collectionTableInfos.firstOrNull { it.member === member }
    }

    /**
     * 删除容器子表中指定 FK 的所有数据
     */
    private fun deleteCollectionByFk(info: CollectionTableInfo, parentId: Any) {
        executor.setupQuoter()
        val sql = "DELETE FROM ${info.tableName.asFormattedColumnName()} WHERE ${info.fkColumnName.asFormattedColumnName()} = ?"
        executor.withConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setObject(1, executor.convertParam(parentId))
                stmt.executeUpdate()
            }
        }
    }

    /**
     * 序列化集合元素：优先使用注册的普通 CustomType，否则 toString()
     */
    private fun serializeCollectionElement(value: Any?, elementType: Class<*>?): String? {
        if (value == null) return null
        if (elementType != null) {
            val ct = CustomTypeFactory.getCustomTypeByClass(elementType)
            if (ct != null) return ct.serialize(value).toString()
        }
        return value.toString()
    }

    /**
     * 反序列化集合元素：优先使用注册的普通 CustomType，否则原样返回
     */
    private fun deserializeCollectionElement(value: Any?, elementType: Class<*>?): Any? {
        if (value == null) return null
        if (elementType != null) {
            val ct = CustomTypeFactory.getCustomTypeByClass(elementType)
            if (ct != null) return ct.deserialize(value)
        }
        return value
    }

    /**
     * 插入容器值到子表
     */
    private fun insertCollectionValues(info: CollectionTableInfo, member: AnalyzedClassMember, parentId: Any, collection: Any) {
        executor.setupQuoter()
        when {
            member.isMap -> {
                val map = collection as? Map<*, *> ?: return
                if (map.isEmpty()) return
                val sql = "INSERT INTO ${info.tableName.asFormattedColumnName()} (${info.fkColumnName.asFormattedColumnName()}, ${"map_key".asFormattedColumnName()}, ${"map_value".asFormattedColumnName()}) VALUES (?, ?, ?)"
                executor.withConnection { conn ->
                    conn.prepareStatement(sql).use { stmt ->
                        map.forEach { (k, v) ->
                            stmt.setObject(1, executor.convertParam(parentId))
                            stmt.setObject(2, serializeCollectionElement(k, member.mapKeyType))
                            stmt.setObject(3, serializeCollectionElement(v, member.collectionElementType))
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                    }
                }
            }
            member.isList -> {
                val list = collection as? List<*> ?: return
                if (list.isEmpty()) return
                val sql = "INSERT INTO ${info.tableName.asFormattedColumnName()} (${info.fkColumnName.asFormattedColumnName()}, ${"value".asFormattedColumnName()}, ${"sort_order".asFormattedColumnName()}) VALUES (?, ?, ?)"
                executor.withConnection { conn ->
                    conn.prepareStatement(sql).use { stmt ->
                        list.forEachIndexed { index, v ->
                            stmt.setObject(1, executor.convertParam(parentId))
                            stmt.setObject(2, serializeCollectionElement(v, member.collectionElementType))
                            stmt.setObject(3, index)
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                    }
                }
            }
            member.isSet -> {
                val set = collection as? Set<*> ?: return
                if (set.isEmpty()) return
                val sql = "INSERT INTO ${info.tableName.asFormattedColumnName()} (${info.fkColumnName.asFormattedColumnName()}, ${"value".asFormattedColumnName()}) VALUES (?, ?)"
                executor.withConnection { conn ->
                    conn.prepareStatement(sql).use { stmt ->
                        set.forEach { v ->
                            stmt.setObject(1, executor.convertParam(parentId))
                            stmt.setObject(2, serializeCollectionElement(v, member.collectionElementType))
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                    }
                }
            }
        }
    }

    /**
     * 批量查询容器子表数据
     */
    private fun batchLoadCollection(
        info: CollectionTableInfo,
        member: AnalyzedClassMember,
        ids: List<Any>
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val idArray = ids.map { executor.convertParam(it)!! }.toTypedArray()
        val placeholders = idArray.joinToString(", ") { "?" }
        val orderClause = if (member.isList) " ORDER BY ${"sort_order".asFormattedColumnName()}" else ""
        val sql = "SELECT * FROM ${info.tableName.asFormattedColumnName()} WHERE ${info.fkColumnName.asFormattedColumnName()} IN ($placeholders)$orderClause"
        executor.withConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                idArray.forEachIndexed { i, v -> stmt.setObject(i + 1, v) }
                stmt.executeQuery().use { rs ->
                    when {
                        member.isMap -> {
                            while (rs.next()) {
                                val fk = rs.getObject(info.fkColumnName)?.toString() ?: continue
                                val rawKey = rs.getString("map_key")
                                val rawValue = rs.getString("map_value")
                                val key = deserializeCollectionElement(rawKey, member.mapKeyType)
                                val value = deserializeCollectionElement(rawValue, member.collectionElementType)
                                @Suppress("UNCHECKED_CAST")
                                val map = result.getOrPut(fk) { mutableMapOf<Any?, Any?>() } as MutableMap<Any?, Any?>
                                map[key] = value
                            }
                        }
                        member.isList -> {
                            while (rs.next()) {
                                val fk = rs.getObject(info.fkColumnName)?.toString() ?: continue
                                val rawValue = rs.getString("value")
                                val value = deserializeCollectionElement(rawValue, member.collectionElementType)
                                @Suppress("UNCHECKED_CAST")
                                val list = result.getOrPut(fk) { mutableListOf<Any?>() } as MutableList<Any?>
                                list.add(value)
                            }
                        }
                        member.isSet -> {
                            while (rs.next()) {
                                val fk = rs.getObject(info.fkColumnName)?.toString() ?: continue
                                val rawValue = rs.getString("value")
                                val value = deserializeCollectionElement(rawValue, member.collectionElementType)
                                @Suppress("UNCHECKED_CAST")
                                val set = result.getOrPut(fk) { mutableSetOf<Any?>() } as MutableSet<Any?>
                                set.add(value)
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * 创建空容器
     */
    private fun emptyCollection(member: AnalyzedClassMember): Any {
        return when {
            member.isList -> emptyList<Any>()
            member.isSet -> emptySet<Any>()
            member.isMap -> emptyMap<Any, Any>()
            else -> error("Not a collection type")
        }
    }

    /**
     * 根据字段名查找 CollectionTableInfo
     *
     * @param fieldName 字段名（匹配 propertyName 或 name）
     * @param typeCheck 容器类型检查（isMap/isList/isSet）
     */
    private fun resolveCollectionTableInfo(fieldName: String, typeCheck: (AnalyzedClassMember) -> Boolean): CollectionTableInfo {
        return collectionTableInfos.firstOrNull { info ->
            (info.member.propertyName == fieldName || info.member.name == fieldName) && typeCheck(info.member)
        } ?: error("Collection field '$fieldName' not found or type mismatch.")
    }

    /**
     * 通过 Filter 查询父记录 @Id 值
     */
    private fun resolveParentId(filter: Filter.() -> Unit): Any {
        val action = ActionSelect(executor.table.name).apply {
            limit(1)
            where(filter)
        }
        return executor.executeQuery(action) { rs ->
            if (rs.next()) {
                val primaryName = collectionTableInfos.firstOrNull()?.fkColumnName
                    ?.removePrefix("parent_")
                    ?: error("Cannot determine primary key column.")
                rs.getObject(primaryName) ?: error("Parent record primary key is null.")
            } else {
                throw NoSuchElementException("No parent record found matching the given filter.")
            }
        }
    }

    /**
     * ContainerOperator.value() extension 不可在此处直接调用，
     * 提供一个委托方法使用相同逻辑。
     */
    private fun Any.value(): Any {
        return when (this) {
            is IndexedEnum -> this.index
            is Enum<*> -> this.name
            is java.util.UUID -> if (executor.table.host is taboolib.module.database.HostPostgreSQL) this else this.toString()
            is Char -> this.code
            else -> CustomTypeFactory.getCustomType(this)?.serialize(this) ?: this
        }
    }
}
