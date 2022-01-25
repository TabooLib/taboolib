package taboolib.module.database.bridge

import org.bson.Document
import taboolib.module.configuration.SecuredFile

/**
 * @author sky
 * @since 2020-07-03 17:11
 */
class BridgeData {

    val id: String

    var checked = false

    var lastVisit = System.currentTimeMillis()
        private set

    val lastUpdate = HashMap<String, Any?>()

    private var data = SecuredFile()

    constructor(id: String) {
        this.id = id
    }

    constructor(id: String, data: SecuredFile) {
        this.id = id
        this.data = data
        update()
    }

    constructor(id: String, input: Document?) {
        this.id = id
        if (input != null) {
            parse(input.entries, "")
        }
        update()
    }

    fun data(): SecuredFile {
        lastVisit = System.currentTimeMillis()
        return data
    }

    fun update() {
        lastVisit = System.currentTimeMillis()
        lastUpdate.clear()
        lastUpdate.putAll(data.getValues())
    }

    private fun parse(input: Set<Map.Entry<String, Any>>, node: String) {
        input.forEach { (k, v) -> if (v is Document) parse(v.entries, "$node$k.") else data[node + k] = v }
    }
}