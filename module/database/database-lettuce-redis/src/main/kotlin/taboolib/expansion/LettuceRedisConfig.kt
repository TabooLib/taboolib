package taboolib.expansion

import io.lettuce.core.ReadFrom
import io.lettuce.core.RedisURI
import io.lettuce.core.SslOptions
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection
import io.lettuce.core.support.BoundedPoolConfig
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.util.files
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import java.io.File
import kotlin.time.Duration
import kotlin.time.toJavaDuration

private fun parseDuration(value: String?, fieldName: String): Duration {
    if (value == null) error("$fieldName must be set")
    return try {
        Duration.parse(value)
    } catch (e: IllegalArgumentException) {
        error("Invalid duration format for $fieldName: '$value'. Expected ISO-8601 duration (e.g., 'PT10S' for 10 seconds, 'PT1M' for 1 minute)")
    }
}

private fun parseDurationOrNull(value: String?, fieldName: String): Duration? {
    if (value == null) return null
    return try {
        Duration.parse(value)
    } catch (e: IllegalArgumentException) {
        error("Invalid duration format for $fieldName: '$value'. Expected ISO-8601 duration (e.g., 'PT10S' for 10 seconds, 'PT1M' for 1 minute)")
    }
}

class LettuceRedisConfig(val configurationSection: ConfigurationSection) {

    val host = configurationSection.getString("host") ?: error("host must be set")
    val port = configurationSection.getInt("port", 6379)
    val password = configurationSection.getString("password")
    val ssl = configurationSection.getBoolean("ssl")
    val timeout = parseDuration(configurationSection.getString("timeout"), "timeout")
    val database = configurationSection.getInt("database", 0)

    val ioThreadPoolSize = configurationSection.getInt("ioThreadPoolSize")
    val computationThreadPoolSize = configurationSection.getInt("computationThreadPoolSize")

    val autoReconnect = configurationSection.getBoolean("autoReconnect", false)
    val pingBeforeActivateConnection = configurationSection.getBoolean("pingBeforeActivateConnection", true)

    val sslOptions: SslOptions by lazy {
        val truststoreFile = configurationSection.getString("truststoreFile") ?: "default.jks"
        val password = configurationSection.getString("truststorePassword")
        SslOptions.builder()
            .jdkSslProvider()
            .truststore(File(getDataFolder(), truststoreFile), password)
            .build()
    }

    val pool = Pool(configurationSection.getConfigurationSection("pool"))

    class Pool(configurationSection: ConfigurationSection?) {

        val lifo = configurationSection?.getBoolean("lifo", true) ?: true
        val fairness = configurationSection?.getBoolean("fairness", false) ?: false

        val maxTotal = configurationSection?.getInt("maxTotal", 8) ?: 8
        val maxIdle = configurationSection?.getInt("maxIdle", 8) ?: 8
        val minIdle = configurationSection?.getInt("minIdle", 0) ?: 0

        val testOnCreate = configurationSection?.getBoolean("testOnCreate", false) ?: false
        val testOnBorrow = configurationSection?.getBoolean("testOnBorrow", false) ?: false
        val testOnReturn = configurationSection?.getBoolean("testOnReturn", false) ?: false
        val testWhileIdle = configurationSection?.getBoolean("testWhileIdle", false) ?: false

        val maxWaitDuration = parseDurationOrNull(configurationSection?.getString("maxWaitDuration"), "pool.maxWaitDuration")
        val blockWhenExhausted = configurationSection?.getBoolean("blockWhenExhausted", true) ?: true
        val timeBetweenEvictionRuns = parseDurationOrNull(configurationSection?.getString("timeBetweenEvictionRuns"), "pool.timeBetweenEvictionRuns")
        val minEvictableIdleDuration = parseDurationOrNull(configurationSection?.getString("minEvictableIdleDuration"), "pool.minEvictableIdleDuration")
        val softMinEvictableIdleDuration = parseDurationOrNull(configurationSection?.getString("softMinEvictableIdleDuration"), "pool.softMinEvictableIdleDuration")
        val numTestsPerEvictionRun = configurationSection?.getInt("numTestsPerEvictionRun", 3) ?: 3

        fun poolConfig(): GenericObjectPoolConfig<StatefulRedisConnection<String, String>> {
            return GenericObjectPoolConfig<StatefulRedisConnection<String, String>>().apply {
                lifo = this@Pool.lifo
                fairness = this@Pool.fairness

                maxTotal = this@Pool.maxTotal
                maxIdle = this@Pool.maxIdle
                minIdle = this@Pool.minIdle

                testOnCreate = this@Pool.testOnCreate
                testOnBorrow = this@Pool.testOnBorrow
                testOnReturn = this@Pool.testOnReturn
                testWhileIdle = this@Pool.testWhileIdle

                this@Pool.maxWaitDuration?.let { setMaxWait(it.toJavaDuration()) }
                blockWhenExhausted = this@Pool.blockWhenExhausted
                this@Pool.timeBetweenEvictionRuns?.let { timeBetweenEvictionRuns = it.toJavaDuration() }
                this@Pool.minEvictableIdleDuration?.let { minEvictableIdleDuration = it.toJavaDuration() }
                this@Pool.softMinEvictableIdleDuration?.let { softMinEvictableIdleDuration = it.toJavaDuration() }
                numTestsPerEvictionRun = this@Pool.numTestsPerEvictionRun
            }
        }

        fun clusterPoolConfig(): GenericObjectPoolConfig<StatefulRedisClusterConnection<String, String>> {
            return GenericObjectPoolConfig<StatefulRedisClusterConnection<String, String>>().apply {
                lifo = this@Pool.lifo
                fairness = this@Pool.fairness

                maxTotal = this@Pool.maxTotal
                maxIdle = this@Pool.maxIdle
                minIdle = this@Pool.minIdle

                testOnCreate = this@Pool.testOnCreate
                testOnBorrow = this@Pool.testOnBorrow
                testOnReturn = this@Pool.testOnReturn
                testWhileIdle = this@Pool.testWhileIdle

                this@Pool.maxWaitDuration?.let { setMaxWait(it.toJavaDuration()) }
                blockWhenExhausted = this@Pool.blockWhenExhausted
                this@Pool.timeBetweenEvictionRuns?.let { timeBetweenEvictionRuns = it.toJavaDuration() }
                this@Pool.minEvictableIdleDuration?.let { minEvictableIdleDuration = it.toJavaDuration() }
                this@Pool.softMinEvictableIdleDuration?.let { softMinEvictableIdleDuration = it.toJavaDuration() }
                numTestsPerEvictionRun = this@Pool.numTestsPerEvictionRun
            }
        }

        fun slavesPoolConfig(): GenericObjectPoolConfig<StatefulRedisMasterReplicaConnection<String, String>> {
            return GenericObjectPoolConfig<StatefulRedisMasterReplicaConnection<String, String>>().apply {
                lifo = this@Pool.lifo
                fairness = this@Pool.fairness

                maxTotal = this@Pool.maxTotal
                maxIdle = this@Pool.maxIdle
                minIdle = this@Pool.minIdle

                testOnCreate = this@Pool.testOnCreate
                testOnBorrow = this@Pool.testOnBorrow
                testOnReturn = this@Pool.testOnReturn
                testWhileIdle = this@Pool.testWhileIdle

                this@Pool.maxWaitDuration?.let { setMaxWait(it.toJavaDuration()) }
                blockWhenExhausted = this@Pool.blockWhenExhausted
                this@Pool.timeBetweenEvictionRuns?.let { timeBetweenEvictionRuns = it.toJavaDuration() }
                this@Pool.minEvictableIdleDuration?.let { minEvictableIdleDuration = it.toJavaDuration() }
                this@Pool.softMinEvictableIdleDuration?.let { softMinEvictableIdleDuration = it.toJavaDuration() }
                numTestsPerEvictionRun = this@Pool.numTestsPerEvictionRun
            }
        }
    }

    val asyncPool = AsyncPool(configurationSection.getConfigurationSection("asyncPool"))

    class AsyncPool(configurationSection: ConfigurationSection?) {

        val maxTotal = configurationSection?.getInt("maxTotal", 8) ?: 8
        val maxIdle = configurationSection?.getInt("maxIdle", 8) ?: 8
        val minIdle = configurationSection?.getInt("minIdle", 0) ?: 0

        fun poolConfig(): BoundedPoolConfig {
            return BoundedPoolConfig.builder()
                .maxTotal(maxTotal)
                .maxIdle(maxIdle)
                .minIdle(minIdle)
                .build()
        }
    }

    // sentinel
    val enableSentinel = configurationSection.getBoolean("sentinel.enable", false)
    val sentinel by lazy { Sentinel(configurationSection.getConfigurationSection("sentinel")!!) }

    class Sentinel(configurationSection: ConfigurationSection) {

        val masterId = configurationSection.getString("masterId") ?: error("masterId must be set")

        val nodes = configurationSection.getStringList("nodes").map { node ->
            val parts = node.split(":")
            require(parts.size == 2) { "Invalid sentinel node format: '$node'. Expected 'host:port'" }
            Node(parts[0], parts[1].toIntOrNull() ?: error("Invalid port in sentinel node: '$node'"))
        }

        class Node(val host: String, val port: Int)
    }

    // slaves
    val enableSlaves = configurationSection.getBoolean("slaves.enable", false)
    val slaves by lazy { Slaves(configurationSection.getConfigurationSection("slaves")!!) }

    class Slaves(configurationSection: ConfigurationSection) {

        val readFrom = ReadFrom.valueOf((configurationSection.getString("readFrom") ?: error("readFrom must be set")))
    }

    // cluster
    val cluster by lazy { Cluster(configurationSection.getConfigurationSection("cluster")!!) }

    class Cluster(configurationSection: ConfigurationSection) {

        val nodes = files("clusters", "cluster0.yml") {
            val configuration = Configuration.loadFromFile(it)
            Node(configuration)
        }

        val enablePeriodicRefresh = configurationSection.getBoolean("enablePeriodicRefresh", false)
        val refreshPeriod = configurationSection.getString("refreshPeriod")?.let { Duration.parse(it) }
        val enableAdaptiveRefreshTrigger = configurationSection.getEnumList("enableAdaptiveRefreshTrigger", ClusterTopologyRefreshOptions.RefreshTrigger::class.java)
        val adaptiveRefreshTriggersTimeout = configurationSection.getString("adaptiveRefreshTriggersTimeout")?.let { Duration.parse(it) }
        val refreshTriggersReconnectAttempts = configurationSection.getInt("refreshTriggersReconnectAttempts", 5)
        val dynamicRefreshSources = configurationSection.getBoolean("dynamicRefreshSources", true)
        val closeStaleConnections = configurationSection.getBoolean("closeStaleConnections", true)
        val maxRedirects = configurationSection.getInt("maxRedirects", 5)
        val validateClusterNodeMembership = configurationSection.getBoolean("validateClusterNodeMembership", true)

        class Node(private val configurationSection: ConfigurationSection) {
            val host = configurationSection.getString("host") ?: error("host must be set")
            val port = configurationSection.getInt("port", 6379)
            val password = configurationSection.getString("password")
            val ssl = configurationSection.getBoolean("ssl")
            val timeout = parseDuration(configurationSection.getString("timeout"), "cluster node timeout")
            val database = configurationSection.getInt("database", 0)

            // sentinel
            val enableSentinel = configurationSection.getBoolean("sentinel.enable", false)
            val sentinel by lazy { Sentinel(configurationSection.getConfigurationSection("sentinel")!!) }

            class Sentinel(configurationSection: ConfigurationSection) {

                val masterId = configurationSection.getString("masterId") ?: error("masterId must be set")

                val nodes = configurationSection.getStringList("nodes").map { node ->
                    val parts = node.split(":")
                    require(parts.size == 2) { "Invalid sentinel node format: '$node'. Expected 'host:port'" }
                    Node(parts[0], parts[1].toIntOrNull() ?: error("Invalid port in sentinel node: '$node'"))
                }

                class Node(val host: String, val port: Int)
            }

            fun redisURIBuilder(): RedisURI.Builder {
                val builder = RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withSsl(ssl)
                    .withTimeout(timeout.toJavaDuration())
                    .withDatabase(database)

                password?.toCharArray()?.let { builder.withPassword(it) }

                if (enableSentinel) {

                    val sentinel = sentinel

                    builder.withSentinelMasterId(sentinel.masterId)
                    sentinel.nodes.forEach {
                        builder.withSentinel(it.host, it.port)
                    }
                }
                return builder
            }
        }
    }

    fun redisURIBuilder(): RedisURI.Builder {
        val builder = RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withSsl(ssl)
            .withTimeout(timeout.toJavaDuration())
            .withDatabase(database)

        password?.toCharArray()?.let { builder.withPassword(it) }

        if (enableSentinel) {

            val sentinel = sentinel

            builder.withSentinelMasterId(sentinel.masterId)
            sentinel.nodes.forEach {
                builder.withSentinel(it.host, it.port)
            }
        }
        return builder
    }
}