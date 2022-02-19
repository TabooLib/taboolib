package taboolib.module.configuration

import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.core.conversion.Converter

class MapConverter : Converter<Map<*, *>, UnmodifiableConfig> {

    override fun convertToField(config: UnmodifiableConfig): Map<*, *> {
        return config.valueMap()
    }

    override fun convertFromField(map: Map<*, *>): UnmodifiableConfig {
        return (Configuration.fromMap(map) as ConfigSection).root
    }
}