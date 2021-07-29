package taboolib.module.database.bridge

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import java.util.concurrent.ConcurrentHashMap

class BridgeDatabase(val client: String, val database: String) {

    val mongoClient: MongoClient = MongoClients.create(ConnectionString(client))
    val mongoDatabase: MongoDatabase = mongoClient.getDatabase(database)
    val collectionMap = ConcurrentHashMap<String, BridgeCollection>()

    fun getCollection(collection: String, indexType: Index = Index.NONE): BridgeCollection {
        return collectionMap.computeIfAbsent(collection) { BridgeCollection(this, collection, indexType) }
    }

    fun release(collection: String) {
        collectionMap.remove(collection)
    }

    fun releaseId(collection: String, id: String?) {
        val bridgeCollection = collectionMap[collection]
        bridgeCollection?.dataMap?.remove(id)
    }

    fun close() {
        mongoClient.close()
    }
}