package taboolib.module.configuration

import com.electronwill.nightconfig.core.conversion.Converter
import org.tabooproject.reflex.ClassMethod
import java.lang.reflect.Field

/**
 * TabooLib
 * taboolib.module.configuration.InnerConverter
 *
 * @author 坏黑
 * @since 2024/3/31 13:37
 */
class InnerConverter(val toField: ClassMethod, val fromField: ClassMethod) {

    fun getConverter(field: Field, root: Any): Converter<Any, Any> {
        return object : Converter<Any, Any> {

            override fun convertToField(value: Any): Any {
                return toField.invokeStatic(field, value, root)!! // root => ConfigurationSection
            }

            override fun convertFromField(value: Any): Any {
                return fromField.invokeStatic(field, value, root)!! // root => Object
            }
        }
    }
}