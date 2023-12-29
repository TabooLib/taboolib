package taboolib.expansion.lock

import taboolib.common.platform.function.submit
import taboolib.expansion.IRedisConnection


/**
 *  分布式Lock
 */
class Lock(val connection: IRedisConnection, val lockName: String){

    companion object{
        private const val LOCKED = "TRUE"
    }
    var internalLockLeaseTime = 30000L

    fun getLockName(): String {
        return prefixName("tabooredis_lock__lock", lockName)
    }

    private var start = false
    private var watchDog = false

    fun tryLock(): Boolean {
        try {
            val luaScripts = "if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then " +
                "redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end"
            val keys: MutableList<String> = ArrayList()
            val values: MutableList<String> = ArrayList()
            keys.add(getLockName())
            values.add(LOCKED)
            values.add(internalLockLeaseTime.toString())
            connection.eval(luaScripts, keys, values)?.let {
                if (it == 1L) {
                    start = true
                    watchDog = true
                    extended()
                }
                return it == 1L
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun unlock() {
        try {
            val luaScript = "if redis.call('get',KEYS[1]) == false then return 1 " +
                "elseif redis.call('get',KEYS[1]) == ARGV[1] then " +
                "return redis.call('del',KEYS[1]) else return 2 end"
            val eval = connection.eval(luaScript, listOf(getLockName()), listOf(LOCKED))?.toString()
            if (eval != "1") {
                throw RuntimeException("解锁失败,key:$lockName")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("unLocking error, key:$lockName")
        }
    }

    fun extended() {
        if (!start) {
            return
        }
        try {
            submit(async = true, period = 20) {
                if (!connection.contains(getLockName()) || !watchDog) {
                    this.cancel()
                }
                val luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                    "return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end"
                val eval = connection.eval(luaScript, listOf(getLockName()), listOf(LOCKED, internalLockLeaseTime.toString()))?.toString()
                if (eval != "1") {
                    this.cancel()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("延长锁的有效时间发生错误，key:$lockName")
        }
    }

    fun prefixName(prefix: String, name: String): String {
        return if (name.contains("{")) {
            "$prefix:$name"
        } else "$prefix:{$name}"
    }


}
