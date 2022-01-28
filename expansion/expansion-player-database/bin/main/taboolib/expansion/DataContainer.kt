package taboolib.expansion

import taboolib.common.platform.function.submit

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.Vars
 *
 * @author sky
 * @since 2021/8/22 7:51 下午
 */
class DataContainer(val user: String, val database: Database) {

    val source = database[user]

    operator fun set(key: String, value: Any) {
        source[key] = value.toString()
        save(key)
    }

    operator fun get(key: String): String? {
        return source[key]
    }

    fun save(key: String) {
        submit(async = true) { database[user, key] = source[key]!! }
    }

    override fun toString(): String {
        return "DataContainer(user='$user', source=$source)"
    }
}