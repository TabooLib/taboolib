package taboolib.module.configuration

import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.core.conversion.Converter
import java.util.*

class MapConverter : Converter<Map<*, *>, UnmodifiableConfig> {

    override fun convertToField(config: UnmodifiableConfig): Map<*, *> {
        return config.valueMap()
    }

    override fun convertFromField(map: Map<*, *>): UnmodifiableConfig {
        return (Configuration.fromMap(map) as ConfigSection).root
    }
}

class UUIDConverter : Converter<UUID, String> {

    override fun convertToField(value: String): UUID {
        return UUID.fromString(value)
    }

    override fun convertFromField(value: UUID): String {
        return value.toString()
    }
}