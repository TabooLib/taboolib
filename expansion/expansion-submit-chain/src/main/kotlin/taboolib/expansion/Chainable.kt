package taboolib.expansion

interface Chainable<T> {

    val block: () -> T

    suspend fun execute(): T
}