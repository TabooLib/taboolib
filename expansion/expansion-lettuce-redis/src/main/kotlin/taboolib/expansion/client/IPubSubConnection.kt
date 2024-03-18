package taboolib.expansion.client

import taboolib.expansion.LettucePubSubListener

interface IPubSubConnection {

    fun subscribeSync(vararg channels: String)

    fun subscribeAsync(vararg channels: String)

    fun subscribeReactive(vararg channels: String)

    fun addListener(action: LettucePubSubListener)

    fun removeListener(action: LettucePubSubListener)

}