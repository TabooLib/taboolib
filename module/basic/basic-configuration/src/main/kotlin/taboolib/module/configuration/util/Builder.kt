package taboolib.module.configuration.util

import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type

/**
 * 创建一个新的配置对象。
 *
 * @param type 配置类型，默认为 YAML。
 * @param concurrent 是否为并发模式，默认为 true。
 * @param builder 配置构建器。
 * @return 新的配置对象。
 */
fun newConfig(type: Type = Type.YAML, concurrent: Boolean = true, builder: Configuration.() -> Unit): Configuration {
    return Configuration.empty(type, concurrent).also(builder)
}

/**
 * 创建一个新的 JSON 配置对象。
 *
 * @param concurrent 是否为并发模式，默认为 true。
 * @param builder 配置构建器。
 * @return 新的 JSON 配置对象。
 */
fun newJsonConfig(concurrent: Boolean = true, builder: Configuration.() -> Unit): Configuration {
    return Configuration.empty(Type.JSON_MINIMAL, concurrent).also(builder)
}