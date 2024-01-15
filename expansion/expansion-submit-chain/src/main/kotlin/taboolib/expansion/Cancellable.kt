package taboolib.expansion

open class Cancellable {

    var cancelled = false
        private set

    fun cancel() {
        cancelled = true
    }
}