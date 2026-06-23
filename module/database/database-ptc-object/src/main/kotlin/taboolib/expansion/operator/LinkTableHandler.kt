package taboolib.expansion.operator

import taboolib.expansion.ContainerOperator
import taboolib.expansion.CustomTypeFactory
import taboolib.expansion.IndexedEnum
import taboolib.expansion.orm.AnalyzedClass
import taboolib.expansion.orm.AnalyzedClassMember
import taboolib.expansion.orm.EntityMapper
import taboolib.module.database.ActionSelect
import taboolib.module.database.asFormattedColumnName

/**
 * @LinkTable 级联操作处理器
 *
 * 封装关联表的查询（JOIN）和级联写入逻辑。
 */
internal class LinkTableHandler(
    private val classTableMap: Map<Class<*>, String>,
    private val classOperatorMap: Map<Class<*>, ContainerOperator>,
    private val executor: DataExecutor
) {

    /**
     * 级联保存关联对象：存在则更新，不存在则插入（支持无限嵌套）
     *
     * 在主对象写入之前调用，确保关联表中的记录已就绪。
     * 深度优先递归：先保存最深层的关联对象，确保外键引用完整。
     * - 关联类有可变字段 -> upsert（存在则更新，不存在则插入）
     * - 关联类全为 val -> 仅在不存在时插入
     *
     * @param visited 祖先链集合，用于环检测（A->B->A 跳过）
     */
    fun cascadeSaveLinkedObjects(
        typeClass: AnalyzedClass,
        dataList: List<Any>,
        visited: MutableSet<Class<*>> = mutableSetOf()
    ) {
        if (!typeClass.hasLinkMembers) return
        visited.add(typeClass.clazz)
        for (member in typeClass.linkMembers) {
            val linkClass = member.linkTableClass ?: continue
            if (linkClass in visited) continue
            val operator = classOperatorMap[linkClass] ?: continue
            val linkedObjects = dataList.mapNotNull { typeClass.getValue(it, member) }
            if (linkedObjects.isEmpty()) continue
            val linkedTypeClass = AnalyzedClass.of(linkClass)
            // 深度优先：先保存更深层的关联对象
            cascadeSaveLinkedObjects(linkedTypeClass, linkedObjects, visited)
            // 再保存当前层
            val hasMutableFields = linkedTypeClass.members.any { !it.isIgnored && !it.isFinal }
            if (hasMutableFields) {
                operator.upsert(linkedObjects)
            } else {
                val primaryName = linkedTypeClass.primaryMemberName ?: continue
                linkedObjects.forEach { obj ->
                    val id = linkedTypeClass.getPrimaryMemberValue(obj) ?: return@forEach
                    if (!operator.has { primaryName eq id.value() }) {
                        operator.insert(listOf(obj))
                    }
                }
            }
        }
        visited.remove(typeClass.clazz)
    }

    /**
     * 获取实际列名列表（@LinkTable 用 FK 列名）
     */
    fun getColumnNames(typeClass: AnalyzedClass): List<String> {
        return typeClass.members.filter { !it.isIgnored && !it.isCollection }.map { member ->
            if (member.isLinkTable) member.linkTableColumn!! else member.name
        }
    }

    /**
     * 获取实际列值列表（@LinkTable 提取 FK 值，扁平化集合用集合 CustomType 序列化）
     */
    fun getColumnValues(typeClass: AnalyzedClass, data: Any): List<Any?> {
        return typeClass.members.filter { !it.isIgnored && !it.isCollection }.map { member ->
            if (member.isLinkTable) {
                val linkedObj = typeClass.getValue(data, member)
                if (linkedObj != null) {
                    val linkedClass = AnalyzedClass.of(member.linkTableClass!!)
                    linkedClass.getPrimaryMemberValue(linkedObj)?.value()
                } else {
                    null
                }
            } else if (member.isFlattenedCollection) {
                val ct = CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)!!
                val raw = typeClass.getValue(data, member)
                if (raw != null) ct.serialize(raw) else null
            } else {
                typeClass.getValue(data, member)?.value()
            }
        }
    }

    /**
     * 构建带 JOIN 的 ActionSelect（支持无限嵌套关联）
     */
    fun buildJoinSelect(typeClass: AnalyzedClass, extra: ActionSelect.() -> Unit = {}): ActionSelect {
        executor.setupQuoter()
        val tableName = executor.table.name
        val aliasCounter = intArrayOf(0)
        val visited = mutableSetOf(typeClass.clazz)
        val linkTree = buildLinkTree(typeClass, tableName, "", aliasCounter, visited)
        return ActionSelect(tableName).apply {
            val selectRows = mutableListOf<String>()
            // 主表列
            typeClass.columnMembers.forEach { member ->
                selectRows += "$tableName.${member.name}".asFormattedColumnName() + " AS " + member.name.asFormattedColumnName()
            }
            // 递归展开 JOIN 树
            fun processNodes(nodes: List<LinkJoinNode>) {
                for (node in nodes) {
                    node.analyzedClass.columnMembers.forEach { linkedMember ->
                        selectRows += "${node.tableAlias}.${linkedMember.name}".asFormattedColumnName() + " AS " + "${node.columnPrefix}${linkedMember.name}".asFormattedColumnName()
                    }
                    leftJoin("${node.realTableName.asFormattedColumnName()} AS ${node.tableAlias.asFormattedColumnName()}") {
                        on("${node.parentAlias}.${node.member.linkTableColumn}".asFormattedColumnName() eq
                            pre("${node.tableAlias}.${node.analyzedClass.primaryMemberName}".asFormattedColumnName()))
                    }
                    processNodes(node.children)
                }
            }
            processNodes(linkTree)
            rows(*selectRows.toTypedArray())
            extra()
        }
    }

    /**
     * 从 JOIN ResultSet 读取完整对象
     */
    fun <T> readJoinResult(typeClass: AnalyzedClass, rs: java.sql.ResultSet): T {
        val mapper = EntityMapper.of(typeClass.clazz)
        @Suppress("UNCHECKED_CAST")
        return mapper.createInstance(mapper.readWithLinks(rs)) as T
    }

    /**
     * 递归构建 JOIN 树（支持无限嵌套）
     *
     * @param typeClass 当前类的分析结果
     * @param parentAlias 父表的别名（用于 ON 子句）
     * @param prefixChain 列别名前缀链，每深一层追加 `__link__{fk}__`
     * @param aliasCounter 单元素数组，全局递增的表别名计数器
     * @param visited 祖先链集合，用于环检测（A->B->A 跳过）
     */
    private fun buildLinkTree(
        typeClass: AnalyzedClass,
        parentAlias: String,
        prefixChain: String,
        aliasCounter: IntArray,
        visited: MutableSet<Class<*>>
    ): List<LinkJoinNode> {
        val nodes = mutableListOf<LinkJoinNode>()
        for (member in typeClass.linkMembers) {
            val linkClass = member.linkTableClass ?: continue
            if (linkClass in visited) continue
            val linkedAnalyzed = AnalyzedClass.of(linkClass)
            val realTableName = classTableMap[linkClass] ?: error(
                "关联类 ${linkClass.simpleName} 未注册表名。请确保关联类的表已在容器中创建。"
            )
            val alias = "__t${aliasCounter[0]++}"
            val prefix = "${prefixChain}__link__${member.linkTableColumn}__"
            visited.add(linkClass)
            val children = buildLinkTree(linkedAnalyzed, alias, prefix, aliasCounter, visited)
            visited.remove(linkClass)
            nodes.add(LinkJoinNode(linkedAnalyzed, alias, realTableName, prefix, member, parentAlias, children))
        }
        return nodes
    }

    /**
     * JOIN 树节点，描述一个 @LinkTable 关联的递归结构
     */
    private data class LinkJoinNode(
        val analyzedClass: AnalyzedClass,
        val tableAlias: String,
        val realTableName: String,
        val columnPrefix: String,
        val member: AnalyzedClassMember,
        val parentAlias: String,
        val children: List<LinkJoinNode>
    )

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
