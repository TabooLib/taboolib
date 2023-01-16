package taboolib.ioc.typeread

import taboolib.ioc.event.GetTypeReaderEvent

object TypeReadManager {

    val typeReader = HashMap<String, TypeRead>()

    fun getReader(clazz: Class<*>): TypeRead {
        val save = typeReader[clazz.name]
        if (save != null) {
            return save
        }
        typeReader.values.forEach {
            if (it.type.isAssignableFrom(clazz)) {
                return it
            }
        }
        val event = GetTypeReaderEvent(clazz)
        event.call()
        return event.reader ?: error("not type reader for $clazz")
    }

}