package taboolib.expansion.orm

import com.j256.ormlite.dao.Dao
import kotlin.reflect.KProperty

/**
 *  用于获取Dao的代理类
 */
class DaoGetter<K, ID>(val entity: Class<K>, val id: Class<ID>) {

    // 拎出来进行加载 节省性能
    val value by lazy {
        EasyORM.dao[entity.name] as Dao<K, ID>
    }

    operator fun getValue(ref: Any?, property: KProperty<*>): Dao<K, ID> {
        return value
    }

}
