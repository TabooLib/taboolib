package taboolib.module.nms

interface ProxyCallback<T> {

    fun get(instance: Any? = null): T

    fun new(vararg parameters: Any?): T

    operator fun invoke(instance: Any? = null, vararg parameters: Any?): T

}