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

class TypeReaderCollection : TypeRead {

    override val type: Class<*> = Collection::class.java

    override fun readAll(clazz: Class<*>, field: Field, database: IOCDatabase){
        val method = type.getDeclaredMethod("add", Any::class.java)
        val genotype = field.genericType
        if (genotype is ParameterizedType) {
            val pt = genotype.actualTypeArguments
            database.getDataAll().forEach { (t, u) ->
                if (u != null) {
                    val target = Class.forName(pt[0].typeName)
                    val data = SerializationManager.deserialize(u, target, pt[0])
                    val assert = ClassAnalyser.analyse(clazz)
                    val instance = clazz.getInstance()
                    val obj = assert.getField(field.name).get(instance)
                    method.invoke(obj, data)
                }
            }
        }
    }

    override fun writeAll(field: Field, source: Class<*>, database: IOCDatabase) {
        database.resetDatabase()
        field.get(source).let { it as? Collection<*> }?.forEach { element ->
            if (element != null) {
                database.saveData(UUID.randomUUID().toString(), element)
            }
        }
        database.saveDatabase()
    }

    companion object {
        @Awake(LifeCycle.CONST)
        fun init() {
            val list = TypeReaderCollection()
            TypeReadManager.typeReader[list.type.name] = list
        }
    }
}