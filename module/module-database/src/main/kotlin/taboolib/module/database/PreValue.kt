package taboolib.module.database

/**
 * 非占位符值
 *
 * @author sky
 * @since 2021/6/23 10:14 下午
 */
class PreValue(val value: Any) {

    override fun toString(): String {
        return value.toString()
    }
}