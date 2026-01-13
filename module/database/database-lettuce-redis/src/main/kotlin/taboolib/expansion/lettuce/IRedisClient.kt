package taboolib.expansion.lettuce

import java.util.concurrent.CompletableFuture

interface IRedisClient {

    /**
     * 启动 Redis 客户端（异步）
     * @param autoRelease 关服是否自动释放
     * */
    fun start(autoRelease: Boolean = true): CompletableFuture<Void>

    /**
     * 启动 Redis 客户端（同步）
     * @param autoRelease 关服是否自动释放
     * */
    fun startSync(autoRelease: Boolean = true)

    /**
     * 结束 Redis 客户端
     *
     * 如果没开启 autoRelease 必须关服前调用
     * */
    fun stop()
}