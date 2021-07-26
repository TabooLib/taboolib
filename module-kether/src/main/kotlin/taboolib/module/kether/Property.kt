package taboolib.module.kether

abstract class Property {

    abstract fun read(instance: Any, key: String): OperationResult

    abstract fun write(instance: Any, key: String, value: Any?): OperationResult

    class OperationResult(val successful: Boolean, val value: Any? = null)
}