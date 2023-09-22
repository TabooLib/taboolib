package taboolib.module.database

/**
 * 回调函数接口
 *
 * @author sky
 * @since 2021/6/24 1:58 上午
 */
interface Future<T> {

    fun <C> call(func: T.() -> C): C
}