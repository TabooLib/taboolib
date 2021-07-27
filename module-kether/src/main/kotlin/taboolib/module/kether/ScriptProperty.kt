package taboolib.module.kether

import java.io.Serializable

abstract class ScriptProperty(val id: String) : Serializable {

    companion object {

        private const val serialVersionUID = 1L
    }

    abstract fun read(instance: Any, key: String): OperationResult

    abstract fun write(instance: Any, key: String, value: Any?): OperationResult

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScriptProperty) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    class OperationResult(val successful: Boolean, val value: Any? = null) {

        companion object {

            fun successful(value: Any? = null) = OperationResult(true, value)

            fun failed() = OperationResult(false)
        }
    }
}