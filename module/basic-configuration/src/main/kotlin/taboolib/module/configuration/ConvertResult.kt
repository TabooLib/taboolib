package taboolib.module.configuration

/**
 * TabooLib
 * taboolib.module.configuration.ConvertResult
 *
 * @author 坏黑
 * @since 2024/3/31 13:30
 */
sealed class ConvertResult(val isSuccessful: Boolean) {

    /** 成功 */
    class Success(val value: Any?) : ConvertResult(true)

    /** 失败 */
    class Failure(val exception: Throwable? = null) : ConvertResult(false)

    /** 跳过 */
    object Skip : ConvertResult(false)
}