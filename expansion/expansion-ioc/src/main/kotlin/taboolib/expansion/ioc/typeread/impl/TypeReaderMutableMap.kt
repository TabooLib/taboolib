package taboolib.expansion.ioc.typeread.impl

import org.tabooproject.reflex.ClassAnalyser
import taboolib.common.LifeCycle
import taboolib.common.io.getInstance
import taboolib.common.platform.Awake
import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.serialization.SerializationManager
import taboolib.expansion.ioc.typeread.TypeRead
import taboolib.expansion.ioc.typeread.TypeReadManager
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.*

//暂时只支持<String,Data>这种结构 其他的暂且不支持 等待大佬帮忙
class TypeReaderMutableMap : TypeRead {

    override val type: Class<*> = MutableMap::class.java

    override fun readAll(clazz: Class<*>, field: Field, database: IOCDatabase) {
        val method = type.getDeclaredMethod("put", Any::class.java, Any::class.java)
        val genotype = field.genericType
        if (genotype is ParameterizedType) {
            val pt = genotype.actualTypeArguments
            database.getDataAll().forEach { (t, u) ->
                if (u != null) {
                    val target = Class.forName(pt[1].typeName)
                    val data = SerializationManager.deserialize(u, target, pt[1])
                    val assert = ClassAnalyser.analyse(clazz)
                    val instance = clazz.getInstance()
                    val obj = assert.getField(field.name).get(instance)
                    method.invoke(obj, t, data)
                }
            }
        }
    }

    override fun writeAll(field: Field, source: Class<*>, database: IOCDatabase) {
        database.resetDatabase()
        field.get(source).let { it as? MutableMap<*, *> }?.forEach { element ->
            if (element.key != null && element.value != null) {
                database.saveData(element.key.toString(), element.value!!)
            }
        }
        database.saveDatabase()
    }

    companion object {
        @Awake(LifeCycle.CONST)
        fun init() {
            val list = TypeReaderMutableMap()
            TypeReadManager.typeReader[list.type.name] = list
        }
    }
}