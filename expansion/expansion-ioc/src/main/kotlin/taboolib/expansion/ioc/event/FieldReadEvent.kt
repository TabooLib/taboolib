package taboolib.expansion.ioc.event

import taboolib.common.platform.event.ProxyEvent
import taboolib.ioc.database.IOCDatabase
import java.lang.reflect.Field

class FieldReadEvent(
    val clazz: Class<*>,
    val field: Field,
    var iocDatabase: IOCDatabase
) : ProxyEvent()