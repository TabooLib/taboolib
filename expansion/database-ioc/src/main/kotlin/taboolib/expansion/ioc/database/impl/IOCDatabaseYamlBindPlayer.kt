package taboolib.expansion.ioc.database.impl

import taboolib.expansion.ioc.database.IOCDatabase
import java.util.*

open class IOCDatabaseYamlBindPlayer : IOCDatabaseMultipleYaml() {

    override fun init(clazz: Class<*>): IOCDatabase {
        this.clazz = clazz.name
        return this
    }

    fun onJoinLoadData(player: UUID) {
        getConfig(player.toString())
    }

    fun onLeaveSaveData(player: UUID) {
        saveData(player.toString())
    }

}