package taboolib.expansion.ioc.typeread

import taboolib.ioc.database.IOCDatabase
import java.lang.reflect.Field

interface TypeRead {

    val type: Class<*>

    fun readAll(field: Field, database: IOCDatabase): Any

    fun writeAll(field: Field, source: Class<*>, database: IOCDatabase)

}