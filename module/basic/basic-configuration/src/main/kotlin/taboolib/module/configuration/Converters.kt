package taboolib.module.configuration

import com.electronwill.nightconfig.core.conversion.Converter
import java.util.*

/**
 * 用于在 Map 和 UnmodifiableConfig 之间进行转换的转换器。
 */
class MapConverter : Converter<Map<*, *>, Map<*, *>> {

    override fun convertToField(config: Map<*, *>): Map<*, *> {
        return config
    }

    override fun convertFromField(map: Map<*, *>): Map<*, *> {
        return map
    }
}

/**
 * 用于在 UUID 和 String 之间进行转换的转换器。
 *
 * @property UUID 源 UUID 类型
 * @property String 目标字符串类型
 */
class UUIDConverter : Converter<UUID, String> {

    override fun convertToField(value: String): UUID {
        return UUID.fromString(value)
    }

    override fun convertFromField(value: UUID): String {
        return value.toString()
    }
}