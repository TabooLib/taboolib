package taboolib.expansion

import io.lettuce.core.pubsub.RedisPubSubListener

fun pubSubListener(action: LettucePubSubListener.() -> Unit): LettucePubSubListener {
    return LettucePubSubListener().also(action)
}

class LettucePubSubListener : RedisPubSubListener<String, String> {

    var onMessage: ((channel: String, message: String) -> Unit) = { _, _ -> }
    override fun message(channel: String, message: String) {
        onMessage.invoke(channel, message)
    }

    var onMessagePattern: ((pattern: String, channel: String, message: String) -> Unit) = { _, _, _ -> }
    override fun message(pattern: String, channel: String, message: String) {
        onMessagePattern.invoke(pattern, channel, message)
    }

    var onSubscribed: ((channel: String, count: Long) -> Unit) = { _, _ -> }
    override fun subscribed(channel: String, count: Long) {
        onSubscribed.invoke(channel, count)
    }

    var onPSubscribed: ((pattern: String, count: Long) -> Unit) = { _, _ -> }
    override fun psubscribed(pattern: String?, count: Long) {
        onPSubscribed.invoke(pattern!!, count)
    }

    var onUnsubscribed: ((channel: String, count: Long) -> Unit) = { _, _ -> }
    override fun unsubscribed(channel: String, count: Long) {
        onUnsubscribed.invoke(channel, count)
    }

    var onPunsubscribed: ((pattern: String, count: Long) -> Unit) = { _, _ -> }
    override fun punsubscribed(pattern: String, count: Long) {
        onPunsubscribed.invoke(pattern, count)
    }
}