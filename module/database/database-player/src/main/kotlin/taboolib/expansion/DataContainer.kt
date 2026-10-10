package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submitAsync
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 缓存优先容器 Cache-First
 * 用于缓存数据并在一定时间后写入数据库
 * 数据库数据不同步给缓存
 *
 * @property user 用户标识
 * @property database 数据库实例
 */
class DataContainer(val user: String, val database: Database) {

    /** 存储用户数据的源 */
    val source = database[user]

    /** 存储需要更新的键值对及其更新时间 */
    val updateMap = ConcurrentHashMap<String, Long>()

    private val writeStates = ConcurrentHashMap<String, WriteState>()

    internal var asyncExecutor: ((() -> Unit) -> Unit) = { task ->
        submitAsync { task() }
    }

    /**
     * 设置指定键的值并立即保存
     *
     * @param key 键
     * @param value 值
     */
    operator fun set(key: String, value: Any) {
        val stringValue = value.toString()
        updateValue(key, stringValue.takeUnless { it.isEmpty() }, deadline = null, updateSource = true)
    }

    /**
     * 穿透缓存的写数据库方法
     *
     * @param targetUser 目标用户
     * @param key 键
     * @param value 值
     * @param sync 是否同步给内存，要求targetUser为UUID
     */
    fun forcedSet(targetUser: String, key: String, value: Any, sync: Boolean = false) {
        val stringValue = value.toString()
        database[targetUser, key] = stringValue
        if (sync) {
            runCatching { UUID.fromString(targetUser) }.getOrNull()?.let { uniqueId ->
                playerDataContainer[uniqueId]?.set(key, stringValue)
            }
        }
    }

    /**
     * 设置指定键的值，并在指定延迟后更新
     *
     * @param key 键
     * @param value 值
     * @param delay 延迟时间
     * @param timeUnit 时间单位
     */
    fun setDelayed(key: String, value: Any, delay: Long = 3L, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        val stringValue = value.toString()
        val deadline = deadlineAfter(timeUnit.toMillis(delay))
        updateValue(key, stringValue.takeUnless { it.isEmpty() }, deadline, updateSource = true)
    }

    /**
     * 获取指定键的值
     *
     * @param key 键
     * @return 对应的值，如果不存在则返回 null
     */
    operator fun get(key: String): String? {
        return source[key]
    }

    /**
     * 获取所有键的集合
     *
     * @return 键的集合
     */
    fun keys(): Set<String> {
        return source.keys
    }

    /**
     * 获取所有键值对
     *
     * @return 键值对映射
     */
    fun values(): Map<String, String> {
        return source
    }

    /**
     * 获取键值对的数量
     *
     * @return 键值对的数量
     */
    fun size(): Int {
        return source.size
    }

    /**
     * 保存指定键的值到数据库
     *
     * @param key 键
     */
    fun save(key: String) {
        val state = writeStates.computeIfAbsent(key) { WriteState() }
        synchronized(state) {
            state.revision++
            state.value = source[key]
            state.deadline = null
            state.ready = true
            updateMap.remove(key)
            if (state.startIfNeeded()) {
                scheduleWrite(key, state)
            }
        }
    }

    /**
     * 从数据库执行删除指定的键操作
     */
    fun delete(key: String) {
        updateValue(key, value = null, deadline = null, updateSource = false)
    }

    /**
     * 检查并更新需要保存的键值对
     */
    fun checkUpdate() {
        val currentTime = System.currentTimeMillis()
        writeStates.forEach { (key, state) ->
            synchronized(state) {
                val deadline = state.deadline
                if (deadline != null && deadline <= currentTime) {
                    state.deadline = null
                    state.ready = true
                    updateMap.remove(key, deadline)
                    if (state.startIfNeeded()) {
                        scheduleWrite(key, state)
                    }
                }
            }
        }
    }

    private fun updateValue(key: String, value: String?, deadline: Long?, updateSource: Boolean) {
        val state = writeStates.computeIfAbsent(key) { WriteState() }
        synchronized(state) {
            if (updateSource) {
                if (value == null) {
                    source.remove(key)
                } else {
                    source[key] = value
                }
            }
            state.revision++
            state.value = value
            state.deadline = deadline
            state.ready = deadline == null
            if (deadline == null) {
                updateMap.remove(key)
            } else {
                updateMap[key] = deadline
            }
            if (state.startIfNeeded()) {
                scheduleWrite(key, state)
            }
        }
    }

    private fun scheduleWrite(key: String, state: WriteState) {
        try {
            asyncExecutor.invoke {
                drainWrites(key, state)
            }
        } catch (ex: Throwable) {
            synchronized(state) {
                state.running = false
            }
            throw ex
        }
    }

    private fun drainWrites(key: String, state: WriteState) {
        while (true) {
            val snapshot = synchronized(state) {
                if (!state.ready) {
                    state.running = false
                    return
                }
                WriteSnapshot(state.revision, state.value)
            }
            try {
                if (snapshot.value == null) {
                    database.remove(user, key)
                } else {
                    database[user, key] = snapshot.value
                }
            } catch (ex: Throwable) {
                synchronized(state) {
                    val hasNewerValue = state.revision != snapshot.revision && state.ready
                    state.running = false
                    if (hasNewerValue) {
                        state.running = true
                        runCatching { scheduleWrite(key, state) }.exceptionOrNull()?.let(ex::addSuppressed)
                    }
                }
                throw ex
            }
            val shouldContinue = synchronized(state) {
                when {
                    state.revision == snapshot.revision -> {
                        state.ready = false
                        state.running = false
                        false
                    }
                    state.ready -> true
                    else -> {
                        state.running = false
                        false
                    }
                }
            }
            if (!shouldContinue) {
                return
            }
        }
    }

    private fun deadlineAfter(delayMillis: Long): Long {
        val currentTime = System.currentTimeMillis()
        return if (delayMillis > 0 && currentTime > Long.MAX_VALUE - delayMillis) {
            Long.MAX_VALUE
        } else {
            currentTime + delayMillis
        }
    }

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    override fun toString(): String {
        return "DataContainer(user='$user', source=$source)"
    }

    private class WriteState {

        var revision = 0L
        var value: String? = null
        var deadline: Long? = null
        var ready = false
        var running = false

        fun startIfNeeded(): Boolean {
            return if (ready && !running) {
                running = true
                true
            } else {
                false
            }
        }
    }

    private data class WriteSnapshot(val revision: Long, val value: String?)

    /**
     * 内部伴生对象，用于定期检查更新
     */
    @Inject
    internal companion object {

        /**
         * 定期检查并更新所有 DataContainer 实例
         */
        @Schedule(period = 20)
        fun checkUpdate() {
            playerDataContainer.entries.forEach { it.value.checkUpdate() }
        }
    }
}
