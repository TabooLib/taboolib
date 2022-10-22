package taboolib.expansion

import taboolib.library.configuration.ConfigurationSection

/**
 * 从配置文件中获取 Redis 连接器
 *
 * @param config 配置文件
 * @return [SingleRedisConnector]
 */
fun SingleRedisConnector.fromConfig(config: ConfigurationSection): SingleRedisConnector {
    host(config.getString("host", "localhost")!!)
    port(config.getInt("port", 6379))
    auth(config.getString("auth"))
    connect(config.getInt("connect", 32))
    timeout(config.getInt("timeout", 1000))
    return this
}