package taboolib.module.kether.action.transform

import taboolib.common.OpenResult
import taboolib.module.kether.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * TabooLib
 * taboolib.module.kether.action.transform.ActionMatcher
 *
 * @author 坏黑
 * @since 2022/9/3 17:22
 */
internal object ActionMatcher {

    @KetherParser(["match"])
    fun actionMatch() = combinationParser {
        it.group(text(), command("by", "with", "using", then = text())).apply(it) { text, pattern ->
            now { Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).also { m -> m.find() } }
        }
    }

    @KetherProperty(bind = Matcher::class)
    fun propertyMatcher() = object : ScriptProperty<Matcher>("matcher.operator") {

        override fun read(instance: Matcher, key: String): OpenResult {
            return try {
                if (key.isInt()) {
                    OpenResult.successful(instance.group(key.toInt()))
                } else {
                    OpenResult.successful(instance.group(key))
                }
            } catch (ex: Exception) {
                OpenResult.failed()
            }
        }

        override fun write(instance: Matcher, key: String, value: Any?): OpenResult {
            return OpenResult.failed()
        }
    }
}