package taboolib.expansion.orm

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.jdbc.DataSourceConnectionSource
import com.j256.ormlite.table.DatabaseTable
import com.j256.ormlite.table.TableUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.module.database.Database
import taboolib.module.database.Host
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Inject
@Awake
@RuntimeDependencies(
    RuntimeDependency(value = "!com.j256.ormlite:ormlite-core:6.1"),
    RuntimeDependency(value = "!com.j256.ormlite:ormlite-jdbc:6.1"),
)
object EasyORM : ClassVisitor(0), Closeable {

    private lateinit var dataSource: HikariDataSource
    lateinit var connectionSource: DataSourceConnectionSource

    lateinit var databaseHost: Host<*>

    /**
     *  初始化数据库连接，应该在 Enable 及以前完成
     */
    fun init(host: Host<*>, hikariConfig: HikariConfig? = null) {
        databaseHost = host
        dataSource = Database.createDataSource(host, hikariConfig) as HikariDataSource
        connectionSource = DataSourceConnectionSource(dataSource, databaseHost.connectionUrl)
        register()
    }

    /**
     *  数据表列表
     *  key: 表名
     *  value: 表对应的类
     */
    val tables = ConcurrentHashMap<String, ReflexClass>()

    /**
     *  数据表列
     *  key: 数据对象的类
     *  value: 列
     */
    val tablesColumn = ConcurrentHashMap<Class<*>, MutableMap<String, TableColumn>>()

    /**
     *  代表数据表对象的列
     */
    data class TableColumn(
        val owner: Class<*>,
        val fieldName: String,
        val field: ClassField,
        // 原始名字
        val originalName: String,
        // 注解种的名字 没有则为原始名字
        val columnName: String,
        // 处理过的名字
        val columnNameProcessed: String
    )

    /**
     * 数据表的DAO
     * key: 表名
     * value: DAO
     */
    val dao = ConcurrentHashMap<String, Dao<*, *>>()

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

    override fun visitStart(clazz: ReflexClass) {
        if (clazz.hasAnnotation(DatabaseTable::class.java)) {
            val annotation = clazz.getAnnotation(DatabaseTable::class.java)
            val tableName = annotation.enum("tableName", "none")
            if (tableName != "none") {
                tables[tableName] = clazz
            }
        }
    }

    /**
     *  注册
     *  1. 获取表名
     *  2. 注册 Dao 单例对象
     *  3. 判断表是否存在，不存在则创建
     *  4. 注册列
     */
    private fun register() {
        tables.forEach { (name, clazz) ->
            val clazzType = runCatching { Class.forName(clazz.name) }.getOrNull() ?: return@forEach
            // 获取ID
            val idField = clazzType.declaredFields.firstOrNull {
                it.isAnnotationPresent(DatabaseField::class.java) && (
                        it.getAnnotation(DatabaseField::class.java).id ||
                                it.getAnnotation(DatabaseField::class.java).generatedId ||
                                it.getAnnotation(DatabaseField::class.java).uniqueIndex
                        )
            }
            if (idField == null) {
                error("Data table $name ID field not found")
            }
            val id = idField.type
            val type = clazzType
            val createDao = createDaoFromClass(connectionSource, type, id)
            if (!createDao.isTableExists) {
                TableUtils.createTable(connectionSource, clazzType)
            }
            // 开始注册列
            val columnMap = mutableMapOf<String, TableColumn>()
            clazz.structure.fields.forEach { field ->
                if (field.isAnnotationPresent(DatabaseField::class.java)) {
                    val annotation = field.getAnnotation(DatabaseField::class.java)
                    val columnName = annotation.enum("columnName", field.name)
                    columnMap[field.name] = TableColumn(
                        clazzType,
                        field.name,
                        field,
                        field.name,
                        columnName,
                        camelToSnake(columnName)
                    )
                }
            }
            tablesColumn[clazzType] = columnMap
        }
    }

    override fun close() {
        dataSource.close()
    }

    /**
     * 用于骗过编译器的方法
     */
    fun <T, D> createDaoFromClass(connectionSource: DataSourceConnectionSource, clazz: Class<T>, id: Class<D>): Dao<T, D> {
        val createDao = DaoManager.createDao(connectionSource, clazz)
        dao[clazz.name] = createDao as Dao<*, *>
        return createDao as Dao<T, D>
    }

    fun camelToSnake(name: String): String {
        val regex = Regex("([a-z0-9])([A-Z])")
        // 将大写字母与小写字母相连的位置插入下划线，并转换为小写
        return name.replace(regex) { matchResult ->
            "${matchResult.groups[1]!!.value}_${matchResult.groups[2]!!.value}"
        }.lowercase(Locale.getDefault())
    }


}

