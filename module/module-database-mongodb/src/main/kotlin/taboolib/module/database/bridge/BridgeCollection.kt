package taboolib.module.database.bridge

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import org.bson.Document
import org.bson.conversions.Bson
import taboolib.common.platform.ProxyPlayer
import taboolib.module.configuration.SecuredFile
import taboolib.module.configuration.Update
import taboolib.module.configuration.contrastAs
import java.util.concurrent.ConcurrentHashMap

class BridgeCollection constructor(val database: BridgeDatabase, val collection: String, val index: Index = Index.NONE) {

    val dataMap = ConcurrentHashMap<String, BridgeData>()

    private val mongoCollection = database.mongoDatabase.getCollection(collection)
    private val gson = Gson()

    init {
        if (!hasIndex()) {
            mongoCollection.createIndex(Indexes.ascending("id"))
        }
    }

    fun release(player: ProxyPlayer) {
        release(if (index == Index.UUID) player.uniqueId.toString() else player.name)
    }

    fun release(id: String) {
        update(id, dataMap.remove(id))
    }

    fun remove(id: String) {
        dataMap.remove(id)
    }

    fun update(player: ProxyPlayer) {
        update(if (index == Index.UUID) player.uniqueId.toString() else player.name)
    }

    fun update(id: String?) {
        if (id != null && dataMap.containsKey(id)) {
            update(id, dataMap[id])
        }
    }

    fun update(id: String, data: BridgeData?) {
        if (data == null) {
            return
        }
        val current = data.data().getValues()
        if (!data.checked && mongoCollection.countDocuments(Filters.eq("id", id)) == 0L) {
            mongoCollection.insertOne(Document().append("id", id))
        }
        val contrast = current.contrastAs(data.lastUpdate)

        if (contrast.isNotEmpty()) {
            mongoCollection.updateOne(Filters.eq("id", id), Updates.combine(toBson(contrast)))
            data.checked = true
            data.lastUpdate.clear()
            data.lastUpdate.putAll(current)
        }
    }

    operator fun get(player: ProxyPlayer): SecuredFile {
        return get(if (index == Index.UUID) player.uniqueId.toString() else player.name, true)
    }

    operator fun get(id: String): SecuredFile {
        return get(id, true)
    }

    operator fun get(id: String, cache: Boolean): SecuredFile {
        if (cache && dataMap.containsKey(id)) {
            return dataMap[id]!!.data()
        }
        val find = mongoCollection.find(Filters.eq("id", id)).first()
        val data: BridgeData
        if (find != null) {
            data = BridgeData(id, find.get("data", Document::class.java))
            data.checked = true
        } else {
            data = BridgeData(id)
        }
        dataMap[id] = data
        return data.data()
    }

    private fun hasIndex(): Boolean {
        return mongoCollection.listIndexes().any { it.get("key", Document::class.java).containsKey("id") }
    }

    private fun toBson(difference: Set<Update>): List<Bson> {
        return difference.map { update ->
            if (update.type == Update.Type.DELETE) {
                Updates.unset("data." + update.node)
            } else {
                Updates.set("data." + update.node, update.value)
            }
        }
    }
}