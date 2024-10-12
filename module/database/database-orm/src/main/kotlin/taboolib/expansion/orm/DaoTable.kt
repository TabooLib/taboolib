package taboolib.expansion.orm

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.misc.BaseDaoEnabled
import java.lang.reflect.ParameterizedType

/**
 *  DaoTable 的代理
 *  将数据类继承此类后
 *  可通过数据对象进行Dao的操作
 */
open class DaoTable<T, ID> : BaseDaoEnabled<T, ID>() {

    val type: Class<T>

    init {
        @Suppress("UNCHECKED_CAST")
        type = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0] as Class<T>

        dao = EasyORM.dao[type.name] as Dao<T, ID>
    }


}
