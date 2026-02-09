package taboolib.expansion

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
 */
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
@Retention(AnnotationRetention.RUNTIME)
annotation class UniqueKey

/**
 * 标记字段不允许为空。
 *
 * **建表行为：**
 * - SQL：为该列添加 NOT NULL 约束
 * - SQLite：为该列添加 NOT NULL 约束
 */
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
@Retention(AnnotationRetention.RUNTIME)
annotation class ColumnType(
    val sql: ColumnTypeSQL = ColumnTypeSQL.VARCHAR,
    val sqlite: ColumnTypeSQLite = ColumnTypeSQLite.TEXT
)