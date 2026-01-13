package taboolib.expansion.util

import taboolib.expansion.LettuceClusterRedisClient
import taboolib.expansion.LettuceRedisClient
import taboolib.expansion.LettuceRedisConfig
import taboolib.library.configuration.ConfigurationSection

/**
 * 根据 [ConfigurationSection] 创建一个 Redis 配置
 *
 * 可以复制 ```example.yml``` 文件里的配置
 * */
fun createLettuceRedisConfig(configurationSection: ConfigurationSection) = LettuceRedisConfig(configurationSection)

/**
 * 根据 [LettuceRedisConfig] 创建一个 集群 Redis 客户端
 *
 * 需要使用 ```IRedisClient.start()``` 方法启动
 * */
fun LettuceRedisConfig.createClusterClient(): LettuceClusterRedisClient = LettuceClusterRedisClient(this)

/**
 * 根据 [LettuceRedisConfig] 创建一个 Redis 客户端
 *
 * 需要使用 ```IRedisClient.start()``` 方法启动
 * */
fun LettuceRedisConfig.createClient(): LettuceRedisClient = LettuceRedisClient(this)