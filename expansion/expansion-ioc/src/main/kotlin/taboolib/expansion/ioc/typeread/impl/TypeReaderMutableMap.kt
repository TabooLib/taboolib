package taboolib.expansion.ioc.typeread.impl

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.typeread.TypeRead
import taboolib.expansion.ioc.typeread.TypeReadManager
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.*

//暂时只支持<String,Data>这种结构 其他的暂且不支持 等待大佬帮忙
class TypeReaderMutableMap : TypeRead {

    override val type: Class<*> = MutableMap::class.java

    override fun readAll(field: Field, database: IOCDatabase) {
        val method = type.getDeclaredMethod("put", Any::class.java, Any::class.java)
        val genotype = field.genericType
        if (genotype is ParameterizedType) {
            val pt = genotype.actualTypeArguments
            database.getDataAll().forEach { (t, u) ->
                if (u != null) {
                    method.invoke(field, t, database.deserialize(t, pt[1].javaClass), pt[1])
                }
            }
        }
    }

    override fun writeAll(field: Field, source: Class<*>, database: IOCDatabase) {
        field.get(source).let { it as? MutableMap<*, *> }?.forEach { element ->
            if (element.key != null && element.value != null) {
                database.saveData(element.key.toString(), element.value!!)
            }
        }
        database.saveDao()
    }

    companion object {
        @Awake(LifeCycle.INIT)
        fun init() {
            val list = TypeReaderMutableMap()
            TypeReadManager.typeReader[list.type.name] = list
        }
    }
}