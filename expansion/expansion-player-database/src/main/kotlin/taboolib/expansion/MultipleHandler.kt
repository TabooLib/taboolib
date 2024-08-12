package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.submitAsync
import taboolib.expansion.AutoDataContainer.Companion.syncTick
import taboolib.library.configuration.ConfigurationSection
import java.util.concurrent.ConcurrentHashMap

/**
 *  多表处理器
 *  可以处理更多数据表
 */
class MultipleHandler(
    val conf: ConfigurationSection,
    table: String = conf.getString("table", pluginId)!!,
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    dataFile: String = "data.db",
    // 是否挂钩玩家的登录与退出, 用于自动创建与销毁数据容器 键是玩家的UUID.toString()
    autoHook: Boolean = false,
    syncTick: Long = 80L,
) {

    val database: Database
    val databaseContainer = ConcurrentHashMap<String, DataContainer>()
    val autoDataContainer = ConcurrentHashMap<String, AutoDataContainer>()

    /**
     *  任务终止同步功能
     */
    private var taskFlag = false

    init {
        database = if (conf.getBoolean("enable")) {
            buildPlayerDatabase(conf, table, flags, clearFlags, ssl)
        } else {
            buildPlayerDatabase(newFile(getDataFolder(), dataFile), table)
        }
        if (autoHook) {
            MultipleHandlerListener.hooks.add(this)
        }
        submitAsync(period = syncTick) {
            if (taskFlag) {
                cancel()
                return@submitAsync
            }
            updateAutoDataContainer()
        }
    }

    /**
     *  停止同步AutoDataContainer
     */
    fun stopSync() {
        taskFlag = true
    }

    /**
     * 重新启动同步AutoDataContainer
     */
    fun restartSync() {
        taskFlag = false
        submitAsync(period = syncTick) {
            if (taskFlag) {
                cancel()
                return@submitAsync
            }
            updateAutoDataContainer()
        }
    }

    /**
     *  初始化数据容器
     *  这个user作为索引，不一定非得是玩家的UUID
     */
    fun setupDataContainer(user: String): DataContainer {
        return databaseContainer.computeIfAbsent(user) { DataContainer(user, database) }
    }

    /**
     *  获取数据容器
     */
    fun getDataContainer(user: String): DataContainer? {
        return databaseContainer[user]
    }

    /**
     *  获取数据库优先数据容器
     */
    fun getAutoDataContainer(user: String): AutoDataContainer {
        return autoDataContainer.computeIfAbsent(user) { AutoDataContainer(user, database) }
    }

    /**
     *  移除数据容器
     */
    fun removeDataContainer(user: String) {
        databaseContainer.remove(user)
    }

    /**
     *  移除数据库优先数据容器
     */
    fun removeAutoDataContainer(user: String) {
        autoDataContainer.remove(user)
    }

    /**
     *  移除两种类型的容器
     */
    fun removeContainer(user: String) {
        removeDataContainer(user)
        removeAutoDataContainer(user)
    }

    /**
     *  更新所有AutoDataContainer
     */
    fun updateAutoDataContainer() {
        autoDataContainer.entries.forEach { it.value.update() }
    }


}
