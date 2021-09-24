package taboolib.expansion

import taboolib.module.database.Host
import taboolib.module.database.Table

abstract class Type {

    abstract fun host(): Host<*>

    abstract fun tableVar(): Table<*, *>
}