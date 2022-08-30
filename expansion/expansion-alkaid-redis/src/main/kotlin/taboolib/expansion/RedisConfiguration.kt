package taboolib.expansion

import taboolib.library.configuration.ConfigurationSection

fun SingleRedisConnector.fromConfig(config: ConfigurationSection): SingleRedisConnector {
    host(config.getString("host", "localhost")!!)
    port(config.getInt("port", 6379))
    auth(config.getString("auth"))
    connect(config.getInt("connect", 32))
    timeout(config.getInt("timeout", 1000))
    return this
}