package taboolib.expansion.tool

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveSettings
import taboolib.common.env.JarRelocation
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.env.RuntimeEnv
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.expansion.AlkaidRedis
import taboolib.expansion.SingleRedisConnection
import taboolib.expansion.SingleRedisConnector
import taboolib.expansion.fromConfig
import taboolib.module.configuration.Configuration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


/**
 *  更快捷的使用 Redis 只需要 link() 即可初始化工具
 *  使用 get() set() del() 来操作缓存
 *  使用 getObject() setCacheObject() 来操作对象缓存 -- 基于 Jackson-Kotlin
 *  使用 setExpire() setEx() 来设置过期时间
 *  使用 delPrefix() 来删除某个前缀的所有key
 *  使用 jsonMapper() 来获取 序列化工具 -- 基于 Jackson-Kotlin
 */
@Inject
@RuntimeDependencies(
    RuntimeDependency(
        "!com.fasterxml.jackson.core:jackson-core:2.17.2",
        test = "!com.fasterxml.jackson.databind.ObjectMapper",
        transitive = false
    ),
    RuntimeDependency(
        "!com.fasterxml.jackson.core:jackson-databind:2.17.2",
        test = "!com.fasterxml.jackson.databind.ObjectMapper",
        transitive = false
    ),
    RuntimeDependency(
        "!com.fasterxml.jackson.core:jackson-annotations:2.17.2",
        test = "!com.fasterxml.jackson.annotation.JsonView",
        transitive = false
    ),
)
object RedisCache {

    @Awake(LifeCycle.LOAD)
    fun load() {
        val rel: MutableList<JarRelocation> = ArrayList()
        if (!PrimitiveSettings.IS_ISOLATED_MODE) {
            rel.add(JarRelocation(RuntimeEnv.KOTLIN_ID + ".", PrimitiveSettings.getRelocatedKotlinVersion() + "."))
            rel.add(JarRelocation(RuntimeEnv.KOTLIN_COROUTINES_ID + ".", PrimitiveSettings.getRelocatedKotlinCoroutinesVersion() + "."))
        }
        RuntimeEnv.ENV_DEPENDENCY.loadDependency("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2", false, rel)
    }

    private var connector: SingleRedisConnector? = null
    private var connection: SingleRedisConnection? = null
    private var isEnable: Boolean = false
    private val cache = ConcurrentHashMap<String, CacheData>()

    data class CacheData(
        var value: String,
        var overTime: Long = -1
    )

    val json by lazy {
        val kotlinModule = KotlinModule.Builder()
            .configure(KotlinFeature.NullIsSameAsDefault, true)
            .build()
        JsonMapper.builder()
            .addModule(kotlinModule)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build()
    }

    // 这里使用了Jackson-Kotlin
    // https://github.com/FasterXML/jackson-module-kotlin
    fun jsonMapper(): ObjectMapper {
        return json
    }

    /**
     * 定时清理过期本地缓存
     */
    @Schedule(delay = 1200, period = 1200, async = true)
    fun check() {
        if (!isEnable) {
            cache.keys.forEach {
                if ((cache[it]?.overTime ?: 0) < System.currentTimeMillis()) {
                    cache.remove(it)
                }
            }
        }
    }

    /**
     * 链接 Redis
     * @param configuration 配置文件
     * redis:
     *   # 是否启用redis 不启用则使用内部缓存
     *   enable: false
     *   host: localhost
     *   port: 6379
     *   #使用账号密码模式时取消注释
     *   #user: user
     *   #password: password
     *   connect: 32
     *   timeout: 1000
     */
    fun link(configuration: Configuration) {
        val redis = configuration.getConfigurationSection("redis")!!
        if (redis.getBoolean("enable")) {
            try {
                connector = AlkaidRedis.create().fromConfig(redis)
                connection?.close()
                connection = connector!!.connect().connection()
                connection!!.setEx("test", "test", 1, TimeUnit.SECONDS)
                isEnable = true
            } catch (_: Exception) {
            }
        }
    }

    /**
     * 安全获取connection失败要重连
     */
    private fun getConnection(): SingleRedisConnection {
        return try {
            connection!!
        } catch (_: Exception) {
            connection = connector!!.connect().connection()
            connection!!
        }
    }

    /**
     * 获取缓存
     */
    fun get(key: String): String? {
        return if (isEnable) {
            getConnection()[key]
        } else {
            cache[key]?.let {
                if (it.overTime == -1L || it.overTime > System.currentTimeMillis()) {
                    it.value
                } else {
                    cache.remove(key)
                    null
                }
            }
        }
    }

    /**
     * 获取缓存对象
     */
    inline fun <reified T> getObject(key: String): T? {
        val value = get(key) ?: return null
        return jsonMapper().readValue(value, T::class.java)
    }

    /**
     * 设置不过期的缓存
     */
    fun set(key: String, value: String) {
        if (isEnable) {
            getConnection()[key] = value
        } else {
            cache[key] = CacheData(value)
        }
    }

    /**
     * 设置缓存对象
     */
    fun setCacheObject(key: String, value: Any) {
        set(key, jsonMapper().writeValueAsString(value))
    }

    /**
     * 设置缓存对象
     */
    fun setCacheObject(key: String, value: Any, seconds: Long, timeUnit: TimeUnit) {
        setEx(key, jsonMapper().writeValueAsString(value), seconds, timeUnit)
    }

    /**
     * 删除缓存
     */
    fun del(key: String) {
        if (isEnable) {
            getConnection().delete(key)
        } else {
            cache.remove(key)
        }
    }

    /**
     * 删除某个前缀的所有key
     */
    fun delPrefix(prefix: String) {
        if (isEnable) {
            val script = """
                local cursor = "0"
                repeat
                    local result = redis.call('SCAN', cursor, 'MATCH', ARGV[1])
                    cursor = result[1]
                    local keys = result[2]
                    for i, key in ipairs(keys) do
                        redis.call('DEL', key)
                    end
                until cursor == "0"
            """.trimIndent()

            getConnection().eval(script, listOf("${prefix}*"), emptyList())
        } else {
            cache.keys.forEach {
                if (it.startsWith(prefix)) {
                    cache.remove(it)
                }
            }
        }
    }

    /**
     * 设置过期时间
     */
    fun setExpire(key: String, seconds: Long, timeUnit: TimeUnit) {
        if (isEnable) {
            getConnection().expire(key, seconds, timeUnit)
        } else {
            cache[key]?.overTime = System.currentTimeMillis() + timeUnit.toMillis(seconds)
        }
    }

    /**
     * 设置带过期时间的缓存
     */
    fun setEx(key: String, value: String, seconds: Long, timeUnit: TimeUnit) {
        if (isEnable) {
            getConnection().setEx(key, value, seconds, timeUnit)
        } else {
            cache[key] = CacheData(value, System.currentTimeMillis() + timeUnit.toMillis(seconds))
        }
    }

}
