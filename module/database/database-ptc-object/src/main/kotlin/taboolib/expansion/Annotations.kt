package taboolib.expansion

import taboolib.module.database.ColumnTypePostgreSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.ColumnTypeSQLite

/**
 * 标记数据类的逻辑主键字段。
 *
 * 框架的 CRUD 操作（find、update、delete、has）均通过 @Id 字段定位记录。
 *
 * **建表行为：**
 * - 若数据类中存在 @Id 字段，该字段在 SQL 中会被设置为 KEY（索引），在 SQLite 中会被设置为 PRIMARY KEY
 * - 若数据类中不存在 @Id 字段，框架会自动添加一个名为 `id` 的自增主键列
 *
 * **⚠ SQLite 与 SQL 的行为差异：**
 * - SQL（MySQL）中 @Id 仅创建 KEY（普通索引），**不强制唯一**，允许同一 @Id 值存在多条记录
 * - SQLite 中 @Id 创建 PRIMARY KEY，**强制唯一**，同一 @Id 值只能存在一条记录
 * - 因此，@Key 复合定位模式（同一 @Id + 不同 @Key 区分多条记录）仅在 SQL 模式下可用；
 *   在 SQLite 模式下，插入相同 @Id 值的第二条记录会抛出 UNIQUE constraint failed 异常
 *
 * **CRUD 行为：**
 * - `find(id)` / `findOne(id)` — 以 @Id 字段值作为 WHERE 条件查询
 * - `update(data)` — 以 @Id 字段值定位记录并更新 var 字段
 * - `delete(id)` — 以 @Id 字段值定位记录并删除
 * - `has(id)` — 以 @Id 字段值检查记录是否存在
 * - `upsert` — 以 @Id（+ @Key）判断记录是否存在，存在则更新，不存在则插入
 *
 * **约束：** 每个数据类最多标记一个 @Id 字段。
 *
 * ```kotlin
 * data class PlayerHome(
 *     @Id val username: UUID,   // 逻辑主键，用于定位记录
 *     var world: String,
 * )
 * ```
 *
 * **兼容性说明：** 同时支持 FIELD、PROPERTY 和 VALUE_PARAMETER 三种注解目标。
 * Kotlin 对 body property 上的注解优先放到 property 目标（Java Field 反射不可见），
 * 框架通过 [AnalyzedClassMember] 的 `FieldWithPropertyAnnotations` 兜底检查
 * Kotlin 生成的 `$annotations` 合成方法，确保 `@Id var x` 和 `@field:Id var x` 均可识别。
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Id

/**
 * 标记数据类的索引字段，同时作为 updateByKey / upsert 的复合定位条件。
 *
 * **建表行为：**
 * - SQL：为该列创建 KEY（普通索引），加速查询
 * - SQLite：通过 `CREATE INDEX` 为该列创建索引
 *
 * **CRUD 行为：**
 * - `updateByKey(data)` — 在 @Id 的基础上，追加所有 @Key 字段作为 WHERE 条件，精确定位记录
 * - `upsert(dataList)` — 以 @Id + 所有 @Key 字段的组合值判断记录是否存在
 *
 * **典型场景：** 当 @Id 不唯一时（如同一玩家在多个服务器有数据），
 * 用 @Key 补充定位条件，形成复合键。
 *
 * **⚠ SQLite 限制：** 此复合定位模式（同一 @Id + 不同 @Key）仅在 SQL（MySQL）模式下可用。
 * SQLite 中 @Id 是 PRIMARY KEY（唯一约束），不允许同一 @Id 值存在多条记录。
 *
 * 允许标记多个 @Key 字段。
 *
 * ```kotlin
 * data class PlayerHome(
 *     @Id val username: UUID,
 *     @Key @Length(32) val serverName: String,  // 索引 + 复合定位
 *     var world: String,
 * )
 * // updateByKey(home) → WHERE username = ? AND server_name = ?
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Key

/**
 * 标记数据类的唯一索引字段。
 *
 * **建表行为：**
 * - SQL：为该列创建 UNIQUE KEY（唯一索引）
 * - SQLite：为该列添加 UNIQUE 约束
 *
 * **注意：** 这是纯数据库层面的约束，框架的 CRUD 操作不会使用 @UniqueKey 进行定位。
 * 如果需要在 updateByKey / upsert 中参与定位，请使用 [@Key]。
 *
 * ```kotlin
 * data class PlayerProfile(
 *     @Id val id: Int,
 *     @UniqueKey @Length(16) val nickname: String,  // 数据库保证昵称不重复
 *     var level: Int,
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UniqueKey

/**
 * 标记字段不允许为空。
 *
 * **建表行为：**
 * - SQL：为该列添加 NOT NULL 约束
 * - SQLite：为该列添加 NOT NULL 约束
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotNull

/**
 * 指定字段在数据库中的存储长度。
 *
 * 主要影响 VARCHAR 类型的列长度，默认值为 64。
 * 当 value 为 -1 时，SQL 模式下字符串字段会使用 LONGTEXT 类型。
 *
 * ```kotlin
 * data class Player(
 *     @Id @Length(36) val uuid: String,
 *     @Length(128) var displayName: String,
 *     @Length(-1) var jsonData: String,  // SQL: LONGTEXT
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Length(val value: Int = 64)

/**
 * 为字段指定数据库中的列名别名。
 *
 * 默认情况下，框架会将驼峰命名转换为下划线命名（如 serverName → server_name）。
 * 使用 @Alias 可以覆盖这一行为，指定自定义列名。
 *
 * ```kotlin
 * data class Player(
 *     @Id val id: Int,
 *     @Alias("display_name") var name: String,  // 列名为 display_name 而非 name
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Alias(val value: String)

/**
 * 显式指定字段的数据库列类型。
 *
 * 当框架的自动类型推断不满足需求时（如需要 LONGTEXT、MEDIUMINT 等特定类型），
 * 可以通过此注解手动指定 SQL 和 SQLite 的列类型。
 *
 * 此注解的优先级高于框架的自动类型推断，会跳过默认的类型映射逻辑。
 *
 * ```kotlin
 * data class Article(
 *     @Id val id: Int,
 *     @ColumnType(sql = ColumnTypeSQL.LONGTEXT) var content: String,
 *     @ColumnType(sql = ColumnTypeSQL.MEDIUMINT) var viewCount: Int,
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ColumnType(
    val sql: ColumnTypeSQL = ColumnTypeSQL.VARCHAR,
    val sqlite: ColumnTypeSQLite = ColumnTypeSQLite.TEXT,
    val postgresql: ColumnTypePostgreSQL = ColumnTypePostgreSQL._DEFAULT
)

/**
 * 标记字段为关联表引用。
 *
 * 当数据类的某个字段类型是另一个数据类时，使用 @LinkTable 声明关联关系。
 * 框架会在主表中创建一个外键列，查询时自动使用 LEFT JOIN 关联查询，
 * 写入时自动提取关联对象的 @Id 值存入外键列。
 *
 * **支持多层嵌套关联**（A→B→C→D→...），框架自动递归展开 JOIN 和级联保存。
 * 框架自动检测循环引用（A→B→A）并跳过，防止无限递归。
 *
 * **建表行为：**
 * - 主表中创建一个外键列（列名由 value 参数指定，自动转为下划线命名）
 * - 外键列的类型与关联类的 @Id 字段类型一致
 *
 * **查询行为：**
 * - 自动生成 LEFT JOIN 查询，关联表的所有列以 `__link__{memberColumnName}__{column}` 为别名
 * - 嵌套关联时前缀链叠加，如 `__link__{fk1}____link__{fk2}__{column}`
 * - 关联对象可能为 null（LEFT JOIN 语义）
 *
 * **写入行为（级联保存）：**
 * - 写入主对象时，自动对非空关联对象执行深度优先级联保存：
 *   - 先保存最深层的关联对象，确保外键引用完整
 *   - 关联对象不存在 → 自动插入到关联表
 *   - 关联对象已存在 → 自动更新关联表中的记录
 * - 主表的外键列存储关联对象的 @Id 值
 *
 * **约束：**
 * - 关联类必须有 @Id 字段
 *
 * ```kotlin
 * data class Guild(@Id val id: String, val name: String, @LinkTable("leaderId") val leader: Player?)
 * data class Player(@Id val id: String, val name: String, @LinkTable("homeId") val home: Home?)
 * data class Home(@Id val id: String, val world: String, var x: Double, var y: Double)
 * // 查询 Guild 时自动 LEFT JOIN player 和 home，递归读取完整对象树
 * ```
 *
 * @param value 外键列的属性名（驼峰命名），框架自动转为下划线列名
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LinkTable(val value: String)

/**
 * 指定数据类对应的数据库表名。
 *
 * 默认情况下，框架会将数据类的类名从驼峰转为下划线命名作为表名（如 PlayerHome → player_home）。
 * 使用 @TableName 可以覆盖这一行为，指定自定义表名。
 *
 * ```kotlin
 * @TableName("my_custom_table")
 * data class PlayerHome(
 *     @Id val username: String,
 *     var world: String,
 * )
 * ```
 *
 * 支持 PostgreSQL Schema：
 * ```kotlin
 * @TableName("player_home", schema = "game")
 * data class PlayerHome(
 *     @Id val username: String,
 *     var world: String,
 * )
 * // 建表时自动创建 Schema：CREATE SCHEMA IF NOT EXISTS "game"
 * // 表名解析为：game.player_home
 * ```
 *
 * @param value 自定义表名
 * @param schema PostgreSQL Schema 名称（为空时不使用 Schema 前缀）
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TableName(val value: String, val schema: String = "")

/**
 * 标记字段为忽略字段，不参与数据库的读写操作。
 *
 * 被 @Ignore 标记的字段不会在数据库中创建列，也不会参与任何 CRUD 操作。
 * 此注解的优先级最高，会覆盖其他所有注解（如 @Id、@Key、@LinkTable 等）。
 *
 * **默认值行为：**
 * - 若字段在构造函数中且 Kotlin 声明了默认值 → 使用 Kotlin 默认值
 * - 若字段在构造函数中但无默认值：
 *   - 可空类型 → null
 *   - List → emptyList()
 *   - Set → emptySet()
 *   - Map → emptyMap()
 *   - 基础类型 → 类型零值（0、false、'\u0000' 等）
 *   - String → ""
 *   - 其他引用类型 → null
 * - 若字段不在构造函数中（字段扫描模式） → 不设置默认值，保留无参构造器的初始值
 *
 * ```kotlin
 * data class PlayerData(
 *     @Id val uuid: String,
 *     var level: Int,
 *     @Ignore val cachedName: String = "unknown",  // 使用 Kotlin 默认值 "unknown"
 *     @Ignore val tempTags: List<String> = listOf() // 使用 Kotlin 默认值 listOf()
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ignore