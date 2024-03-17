package taboolib.expansion.client

import io.lettuce.core.RedisURI
import taboolib.expansion.URIBuilder

interface IRedisMultiple {

    fun connect(vararg uri: RedisURI)

}