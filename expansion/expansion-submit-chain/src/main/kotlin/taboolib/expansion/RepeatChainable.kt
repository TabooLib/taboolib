package taboolib.expansion

interface RepeatChainable<T> {

    val block: Cancellable.() -> T

    suspend fun execute(): T
}