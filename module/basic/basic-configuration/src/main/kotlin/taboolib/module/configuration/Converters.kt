package taboolib.module.configuration

import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.core.conversion.Converter
import java.util.*

/**
 * 用于在 Map 和 UnmodifiableConfig 之间进行转换的转换器。
 *
 * @property Map<*, *> 源映射类型
 * @property UnmodifiableConfig 目标配置类型
 */
class MapConverter : Converter<Map<*, *>, UnmodifiableConfig> {

    override fun convertToField(config: UnmodifiableConfig): Map<*, *> {
        return config.valueMap()
    }

    override fun convertFromField(map: Map<*, *>): UnmodifiableConfig {
        return (Configuration.fromMap(map) as ConfigSection).root
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