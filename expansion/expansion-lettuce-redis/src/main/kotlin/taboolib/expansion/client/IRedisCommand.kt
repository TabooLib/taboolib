package taboolib.expansion.client

import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.api.sync.RedisCommands

// 这里狭义的设置了所有的 K V 都是String类型
// 更多类型支持会让开发逻辑更加复杂
// 一般情况下也不在Redis里加减乘除
interface IRedisCommand {

    // 同步命令源
    var sync: RedisCommands<String, String>

    // 异步命令源
    var async: RedisAsyncCommands<String, String>

    // 反应式命令源
    var reactive: RedisReactiveCommands<String, String>

    fun sync(block: RedisCommands<String, String>.() -> Any): Any {
        return sync.let(block)
    }

    fun async(block: RedisAsyncCommands<String, String>.() -> Any): Any {
        return async.let(block)
    }

    fun reactive(block: RedisReactiveCommands<String, String>.() -> Any): Any {
        return reactive.let(block)
    }

}