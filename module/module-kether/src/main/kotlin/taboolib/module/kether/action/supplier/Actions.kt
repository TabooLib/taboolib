package taboolib.module.kether.action.supplier

import taboolib.common.OpenResult
import taboolib.module.kether.*


/**
 * @author IzzelAliz
 */
internal object Actions {

    @KetherParser(["null"])
    fun parser1() = scriptParser {
        actionNow { null }
    }

    @KetherParser(["pass"])
    fun parser2() = scriptParser {
        actionNow { "" }
    }

    @KetherParser(["vars", "variables"])
    fun parser3() = scriptParser {
        actionNow { deepVars().keys.toList() }
    }

    @KetherProperty(bind = String::class)
    fun propertyString() = object : ScriptProperty<String>("string.operator") {

        override fun read(instance: String, key: String): OpenResult {
            return when (key) {
                "uppercase" -> OpenResult.successful(instance.uppercase())
                "lowercase" -> OpenResult.successful(instance.lowercase())
                "length", "size" -> OpenResult.successful(instance.length)
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: String, key: String, value: Any?): OpenResult {
            return OpenResult.failed()
        }
    }

    @KetherProperty(bind = Map::class)
    fun propertyMap() = object : ScriptProperty<MutableMap<Any, Any?>>("map.operator") {

        override fun read(instance: MutableMap<Any, Any?>, key: String): OpenResult {
            return when {
                key.startsWith("@") -> OpenResult.successful(instance[key.substring(1)])
                key == "length" || key == "size" -> OpenResult.successful(instance.size)
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: MutableMap<Any, Any?>, key: String, value: Any?): OpenResult {
            return if (key.startsWith("@")) {
                if (value != null) {
                    instance[key.substring(1)] = value
                } else {
                    instance.remove(key.substring(1))
                }
                OpenResult.successful()
            } else {
                OpenResult.failed()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @KetherProperty(bind = List::class)
    fun propertyList() = object : ScriptProperty<MutableList<Any?>>("list.operator") {

        override fun read(instance: MutableList<Any?>, key: String): OpenResult {
            return when {
                key.isInt() -> OpenResult.successful(instance[key.toInt()])
                key == "length" || key == "size" -> OpenResult.successful(instance.size)
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: MutableList<Any?>, key: String, value: Any?): OpenResult {
            return if (key.isInt()) {
                instance[key.toInt()] = value
                OpenResult.successful()
            } else {
                OpenResult.failed()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @KetherProperty(bind = Array::class)
    fun propertyArray() = object : ScriptProperty<Array<Any?>>("array.operator") {

        override fun read(instance: Array<Any?>, key: String): OpenResult {
            return when {
                key.isInt() -> OpenResult.successful(instance[key.toInt()])
                key == "length" || key == "size" -> OpenResult.successful(instance.size)
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: Array<Any?>, key: String, value: Any?): OpenResult {
            return if (key.isInt()) {
                instance[key.toInt()] = value
                OpenResult.successful()
            } else {
                OpenResult.failed()
            }
        }
    }
}