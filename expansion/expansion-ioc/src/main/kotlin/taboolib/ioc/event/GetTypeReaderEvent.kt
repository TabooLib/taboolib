package taboolib.ioc.event

import taboolib.common.platform.event.ProxyEvent
import taboolib.ioc.typeread.TypeRead

class GetTypeReaderEvent(
    val clazz: Class<*>,
    var reader: TypeRead? = null
) : ProxyEvent()