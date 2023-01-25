package taboolib.expansion.ioc.event

import taboolib.common.platform.event.ProxyEvent
import taboolib.expansion.ioc.serialization.SerializeFunction

class SerializationGetFunctionEvent(
    val data: Any,
    val targetFlag: String,
    var function: SerializeFunction,
) : ProxyEvent()