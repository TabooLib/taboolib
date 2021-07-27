package taboolib.module.database

interface Future<T> {

    fun <C> call(func: T.() -> C): C
}