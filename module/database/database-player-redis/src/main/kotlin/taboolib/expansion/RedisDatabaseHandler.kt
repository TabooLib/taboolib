package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.pluginId
import taboolib.library.configuration.ConfigurationSection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 *  创建 Redis 数据管理器
 *  配置文件要求如下:
 *  Database:
 *   enable: false
 *   host: localhost
 *   port: 3306
 *   user: root
 *   password: root
 *   database: minecraft
 *   table: elytra
 * Redis:
 *   host: localhost
 *   port: 6379
 *   user: user
 *   password: password
 *   connect: 32
 *   timeout: 1000
 */
class RedisDatabaseHandler(
    val conf: ConfigurationSection,
    var table: String = "",
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    dataFile: String = "data.db",
) : AutoCloseable {

    val database: Database
    private var connector: SingleRedisConnector? = null
    var connection: SingleRedisConnection? = null

    private val closed = AtomicBoolean(false)

    /**
     * 玩家Redis数据容器。
     *
     * 该变量用于存储玩家的数据容器。它是一个线程安全的并发哈希映射，
     * 以玩家的 UUID 为键，对应的 [DataContainer] 为值。
     * 这允许快速、安全地访问和修改玩家的数据。
     */
    val redisDataContainer = ConcurrentHashMap<String, RedisDataContainer>()

    init {
        val databaseConfig = conf.getConfigurationSection("Database")!!
        // 记录本次实际回退到旧位置（配置根节点）的键，用于统一打印一次弃用警告
        val legacyPaths = ArrayList<String>()
        // table 缺失时回退读取根节点，均无则沿用构造参数，最后才落到 pluginId
        val tableSection = resolveSection(databaseConfig, "table", legacyPaths)
        table = tableSection?.getString("table")?.takeIf { it.isNotEmpty() } ?: table.ifEmpty { pluginId }
        // enable 缺失时回退读取根节点；由于旧配置的连接参数同样写在根节点，
        // 因此建库时必须复用「提供 enable 的那个节点」，否则 host/user 等会全部落到默认值
        val enableSection = resolveSection(databaseConfig, "enable", legacyPaths)
        warnLegacyConfig(legacyPaths)
        database = if (enableSection != null && enableSection.getBoolean("enable")) {
            buildPlayerDatabase(enableSection, table, flags, clearFlags, ssl)
        } else {
            buildPlayerDatabase(newFile(getDataFolder(), dataFile), table)
        }
        try {
            val redis = conf.getConfigurationSection("Redis")!!
            if (redis.getBoolean("enable")) {
                val newConnector = AlkaidRedis.create().fromConfig(redis)
                connector = newConnector
                connection = newConnector.connect().connection()
            }
        } catch (ex: Throwable) {
            connector?.close()
            database.close()
            throw ex
        }
        // 登记到 DISABLE 生命周期，插件关闭时自动释放，参考 AlkaidRedis / LettuceRedis 的做法
        register(this)
    }

    /**
     * 解析某个键实际所在的配置节点。
     *
     * 历史版本的实现存在层级错乱：`table` 从 `Database` 子节点读取，`enable` 却从配置根节点读取。
     * 因此为了让 MySQL 真正生效，部分用户已经把 `enable` 连同 `host`、`user` 等一并写在了配置根节点。
     * 这里优先读取新层级（`Database` 子节点），缺失时回退到旧位置（配置根节点），
     * 并把发生回退的键登记进 [legacyPaths] 以便打印弃用警告。
     *
     * 注意必须用 [ConfigurationSection.contains] 判断而非取值判空：
     * `getBoolean` 对不存在的键同样返回 `false`，无法区分「键不存在」与「键存在且显式为 false」。
     *
     * 这是升级过渡期的兼容逻辑，待用户完成配置迁移后可整体移除。
     */
    private fun resolveSection(
        databaseConfig: ConfigurationSection,
        path: String,
        legacyPaths: MutableList<String>,
    ): ConfigurationSection? {
        if (databaseConfig.contains(path)) {
            return databaseConfig
        }
        if (conf.contains(path)) {
            legacyPaths += path
            return conf
        }
        return null
    }

    /**
     * 打印一次配置弃用警告，提示用户把根节点的键迁移到 `Database` 节点下。
     *
     * 同样属于过渡期逻辑，与 [resolveSection] 一并移除。
     */
    private fun warnLegacyConfig(legacyPaths: List<String>) {
        if (legacyPaths.isEmpty()) {
            return
        }
        PrimitiveIO.warning(
            "Deprecated player redis database configuration detected: {0}. " +
                "These options are now read from the \"Database\" section, " +
                "please move them under it (e.g. \"Database.enable\"). " +
                "The fallback to the root section will be removed in a future release.",
            legacyPaths.joinToString(", ")
        )
    }

    /**
     *  初始化数据容器
     *  这个user作为索引，不一定非得是玩家的UUID
     */
    fun setupRedisDataContainer(user: String): RedisDataContainer {
        return redisDataContainer.computeIfAbsent(user) { RedisDataContainer(user, database, this) }
    }

    /**
     *  获取数据容器
     */
    fun getRedisDataContainer(user: String): RedisDataContainer {
        return redisDataContainer[user] ?: error("unavailable database container ${user}")
    }

    /**
     *  移除数据容器
     */
    fun removeRedisDataContainer(user: String) {
        redisDataContainer.remove(user)
    }

    /**
     * 释放 Redis 连接、连接器以及当前处理器拥有的数据库连接池。
     */
    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }
        unregister(this)
        redisDataContainer.clear()
        val currentConnection = connection
        val currentConnector = connector
        connection = null
        connector = null

        var failure: Throwable? = null
        fun closeResource(resource: AutoCloseable?) {
            try {
                resource?.close()
            } catch (ex: Throwable) {
                val firstFailure = failure
                if (firstFailure == null) {
                    failure = ex
                } else {
                    firstFailure.addSuppressed(ex)
                }
            }
        }

        closeResource(currentConnection)
        closeResource(currentConnector)
        closeResource(database)
        failure?.let { throw it }
    }

    /**
     * 生命周期登记表。
     *
     * [RedisDatabaseHandler] 实现了 [AutoCloseable]，但插件通常不会自行调用，
     * 因此在构造时登记，由 DISABLE 阶段统一释放。
     */
    @Inject
    companion object {

        private val handlers = ConcurrentHashMap.newKeySet<RedisDatabaseHandler>()
        private val shutdown = AtomicBoolean(false)

        internal fun register(handler: RedisDatabaseHandler) {
            if (shutdown.get()) {
                runCatching { handler.close() }
                return
            }
            handlers += handler
            // 登记与关闭并发时，二次检查确保迟到的处理器同样被回收
            if (shutdown.get() && handlers.remove(handler)) {
                runCatching { handler.close() }
            }
        }

        internal fun unregister(handler: RedisDatabaseHandler) {
            handlers.remove(handler)
        }

        @Awake(LifeCycle.DISABLE)
        internal fun closeAll() {
            if (!shutdown.compareAndSet(false, true)) {
                return
            }
            handlers.toList().forEach { handler ->
                if (handlers.remove(handler)) {
                    runCatching { handler.close() }.exceptionOrNull()?.let {
                        PrimitiveIO.warning("Failed to close redis database handler {0}: {1}", handler.table, it.toString())
                    }
                }
            }
        }
    }
}
