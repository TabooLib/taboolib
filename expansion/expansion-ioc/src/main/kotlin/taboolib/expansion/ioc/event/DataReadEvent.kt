package taboolib.expansion.ioc.event

import taboolib.common.event.CancelableInternalEvent
import taboolib.expansion.ioc.database.IOCDatabase

class DataReadEvent(var data: Class<*>, var iocDatabase: IOCDatabase) : CancelableInternalEvent()