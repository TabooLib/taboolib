@file:RuntimeDependencies(
    RuntimeDependency(
        value = "!com.google.code.gson:gson:2.8.7",
        test = "!com.google.gson.JsonElement"
    ),
    RuntimeDependency(
        value = "!com.mongodb:MongoDB:3.12.7",
        test = "!com.mongodb.client.MongoClient",
        repository = "https://repo2s.ptms.ink/repository/maven-releases/"
    )
)

package taboolib.module.database.bridge

import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.submit
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.configuration.FileConfiguration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private val databaseMap = ConcurrentHashMap<String, BridgeDatabase>()

fun createBridgeDatabase(client: String, database: String): BridgeDatabase {
    return databaseMap.computeIfAbsent("$client:$database") { BridgeDatabase(client, database) }
}

fun createBridgeCollection(client: String, database: String, collection: String, indexType: Index = Index.NONE): BridgeCollection {
    return createBridgeDatabase(client, database).getCollection(collection, indexType)
}

fun ProxyPlayer.releaseBridge() {
    databaseMap.values.forEach { database ->
        database.collectionMap.forEach {
            if (it.value.index == Index.USERNAME) {
                it.value.release(name)
            } else if (it.value.index == Index.UUID) {
                it.value.release(uniqueId.toString())
            }
        }
    }
}

fun FileConfiguration.toMap(): Map<String, Any> {
    val map = getValues(true)
    map.entries.removeIf { (_, value) -> value is ConfigurationSection }
    return map
}

internal object Releaser {

    @Awake(LifeCycle.ACTIVE)
    fun e() {
        submit(period = 1200, async = true) {
            databaseMap.values.forEach { database ->
                database.collectionMap.forEach {
                    it.value.dataMap.forEach { (key, value) ->
                        if (System.currentTimeMillis() - value.lastVisit > TimeUnit.MINUTES.toMillis(5)) {
                            it.value.release(key)
                        }
                    }
                }
            }
        }
    }
}