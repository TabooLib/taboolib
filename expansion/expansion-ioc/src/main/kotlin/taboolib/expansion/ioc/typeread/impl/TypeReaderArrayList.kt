package taboolib.expansion.ioc.typeread.impl

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.ioc.database.IOCDatabase
import taboolib.ioc.typeread.TypeRead
import taboolib.ioc.typeread.TypeReadManager
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.*

class TypeReaderArrayList : TypeRead {

    override val type: Class<*> = ArrayList::class.java

    override fun readAll(field: Field, database: IOCDatabase) {
        val method = type.getDeclaredMethod("add", Any::class.java)
        val genotype = field.genericType
        if (genotype is ParameterizedType) {
            val pt = genotype.actualTypeArguments
            database.getDataAll().forEach { (t, u) ->
                if (u != null) {
                    //把对象反序列化然后注入到变量里
                    //这里走的是IODatabase里的反序列化 也就是从Dao层获取数据然后存入
                    method.invoke(field, database.deserialize(t, pt[0].javaClass), pt[0])
                }
            }
        }
    }

    override fun writeAll(field: Field, source: Class<*>, database: IOCDatabase) {
        field.get(source).let { it as? ArrayList<*> }?.forEach { element ->
            if (element != null) {
                database.saveData(UUID.randomUUID().toString(), element)
            }
        }
        database.saveDao()
    }

    companion object {
        @Awake(LifeCycle.INIT)
        fun init() {
            val list = TypeReaderArrayList()
            TypeReadManager.typeReader[list.type.name] = list
        }
    }
}