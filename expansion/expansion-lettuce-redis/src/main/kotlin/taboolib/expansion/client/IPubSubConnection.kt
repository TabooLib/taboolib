package taboolib.expansion.client

import taboolib.expansion.LettucePubSubListener

interface IPubSubConnection {

    fun addListener(action: LettucePubSubListener)

    fun removeListener(action: LettucePubSubListener)

}