package taboolib.expansion

interface RepeatChainable {

    val block: Cancellable.() -> Unit

    suspend fun execute()
}