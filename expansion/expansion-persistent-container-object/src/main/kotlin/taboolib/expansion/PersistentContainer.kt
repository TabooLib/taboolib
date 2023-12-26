package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.AnalyzedClassMember.Companion.toColumnName
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigLoader
import taboolib.module.configuration.Configuration
import java.io.File

fun dbFile(file: String = "data.db"): File {
    return newFile(getDataFolder(), file)
}

fun dbSection(file: String = "config.yml", node: String = "database"): ConfigurationSection {
    val conf = ConfigLoader.files[file] ?: return Configuration.empty()
    return conf.configuration.getConfigurationSection(node) ?: Configuration.empty()
}

fun dbSection(section: ConfigurationSection, node: String = "database"): ConfigurationSection {
    return section.getConfigurationSection(node) ?: Configuration.empty()
}

fun db(name: String = "config.yml", node: String = "database", file: String = "data.db"): Any {
    val conf = ConfigLoader.files[name] ?: return dbFile(file)
    return if (conf.configuration.getBoolean("$node.enable")) {
        conf.configuration.getConfigurationSection(node) ?: Configuration.empty()
    } else {
        newFile(getDataFolder(), file)
    }
}

/**
 * 创建持久化储存容器
 */
fun persistentContainer(
    type: Any = db(),
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    builder: PersistentContainer.() -> Unit
): PersistentContainer {
    return PersistentContainer(type, flags, clearFlags, ssl, builder)
}

/**
 * 创建持久化储存容器
 */
fun persistentContainer(
    host: String = "localhost",
    port: Int = 3306,
    user: String = "root",
    password: String = "root",
    database: String = "minecraft",
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    builder: PersistentContainer.() -> Unit,
): PersistentContainer {
    return PersistentContainer(host, port, user, password, database, flags, clearFlags, ssl, builder)
}

class PersistentContainer {

    val container: Container<*>

    /**
     * 设置源
     * - 传入文件类型则为 SQLite 模式
     * - 传入 ConfigurationSection 则读取 SQL 配置
     */
    constructor(
        type: Any,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
        builder: PersistentContainer.() -> Unit
    ) {
        container = when (type) {
            // SQLite 模式
            is File -> {
                ContainerSQLite(type)
            }
            // SQLite 模式
            is String -> {
                ContainerSQLite(newFile(getDataFolder(), type))
            }
            // SQL 模式
            is ConfigurationSection -> {
                ContainerSQL(
                    type.getString("host", "localhost")!!,
                    type.getInt("port"),
                    type.getString("user", "user")!!,
                    type.getString("password", "user")!!,
                    type.getString("database", "minecraft")!!,
                    flags,
                    clearFlags,
                    ssl
                )
            }
            // 无效类型
            else -> error("Unsupported source type: $type")
        }
        builder(this)
        container.init()
    }

    /**
     * 设置 SQL 源
     */
    constructor(
        host: String,
        port: Int,
        user: String,
        password: String,
        database: String,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
        builder: PersistentContainer.() -> Unit
    ) {
        container = ContainerSQL(host, port, user, password, database, flags, clearFlags, ssl)
        builder(this)
        container.init()
    }

    /**
     * 从数据类创建容器
     */
    inline fun <reified T> new(name: String) = new(T::class.java, name)

    /**
     * 从数据类创建容器
     */
    fun <T> new(type: Class<T>, name: String = type.simpleName.toColumnName()) {
        container.createTable(AnalyzedClass.of(type), name)
    }

    /**
     * 获取控制器
     */
    operator fun get(name: String): ContainerOperator {
        return container.operator(name)
    }

    /**
     * 关闭容器
     */
    fun close() {
        container.close()
    }
}

