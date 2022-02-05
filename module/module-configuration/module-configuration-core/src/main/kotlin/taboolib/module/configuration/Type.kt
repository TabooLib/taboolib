package taboolib.module.configuration

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.hocon.HoconFormat
import com.electronwill.nightconfig.json.JsonFormat
import com.electronwill.nightconfig.toml.TomlFormat
import taboolib.internal.YamlFormat

/**
 * TabooLib
 * taboolib.module.configuration.Type
 *
 * @author mac
 * @since 2021/11/21 10:52 下午
 */
enum class Type(private val format: () -> ConfigFormat<out Config>) {

    YAML({ YamlFormat.INSTANCE }),

    TOML({ TomlFormat.instance() }),

    JSON({ JsonFormat.emptyTolerantInstance() }),

    FAST_JSON({ JsonFormat.minimalEmptyTolerantInstance() }),

    HOCON({ HoconFormat.instance() });

    internal fun newFormat(): ConfigFormat<out Config> {
        return format()
    }

    companion object {

        fun getType(format: ConfigFormat<*>): Type {
            return values().first { it.newFormat().javaClass == format.javaClass }
        }
    }
}