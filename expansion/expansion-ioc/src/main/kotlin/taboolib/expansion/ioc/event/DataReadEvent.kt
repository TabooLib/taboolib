package taboolib.expansion.ioc.event

import taboolib.common.platform.event.ProxyEvent
import taboolib.expansion.ioc.database.IOCDatabase

class DataReadEvent(
    var data: Class<*>,
    var iocDatabase: IOCDatabase,
) : ProxyEvent()