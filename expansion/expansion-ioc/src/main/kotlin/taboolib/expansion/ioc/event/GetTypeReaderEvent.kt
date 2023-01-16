package taboolib.expansion.ioc.event

import taboolib.common.platform.event.ProxyEvent
import taboolib.expansion.ioc.typeread.TypeRead

class GetTypeReaderEvent(
    val clazz: Class<*>,
    var reader: TypeRead? = null
) : ProxyEvent()