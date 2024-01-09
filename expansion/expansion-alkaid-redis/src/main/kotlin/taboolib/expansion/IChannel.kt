package taboolib.expansion

import redis.clients.jedis.JedisPubSub

interface IChannel {

    /**
     * 推送信息
     *
     * @param channel 频道
     * @param message 消息
     */
    fun publish(channel: String, message: Any)

    /**
     * 订阅频道
     *
     * @param channel 频道
     * @param patternMode 频道名称是否为正则模式
     * @param func 信息处理函数
     */
    fun subscribe(vararg channel: String, patternMode: Boolean = false, func: RedisMessage.() -> Unit)
    fun createPubSub(patternMode: Boolean, func: RedisMessage.() -> Unit): JedisPubSub

}
