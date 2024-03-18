package taboolib.expansion.client

enum class PoolType {

    // 不使用连接池
    NONE,

    // 使用同步阻塞连接池 基于Pool2
    SYNC,

    // 使用异步非阻塞连接池
    ASYNC

}