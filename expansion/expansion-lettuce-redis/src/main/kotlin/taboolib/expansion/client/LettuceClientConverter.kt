package taboolib.expansion.client

import taboolib.expansion.URIBuilder

data class LettuceClientConverter(val redisURI: URIBuilder) {

    inline fun <reified T : IRedisClient> link(pool: PoolType = PoolType.NONE, builder: T.() -> Unit = {}): T {
        // 通过反射创建实例
        // 构造函数必须是 URIBuilder,第二参数是PoolType 当Pool 作为参数
        val constructor = T::class.java.getConstructor(URIBuilder::class.java, PoolType::class.java)
        val newInstance = constructor.newInstance(redisURI, pool)
        builder.invoke(newInstance as T)
        return newInstance
    }

}