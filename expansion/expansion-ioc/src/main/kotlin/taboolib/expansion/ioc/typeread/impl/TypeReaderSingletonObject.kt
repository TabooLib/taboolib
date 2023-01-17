package taboolib.expansion.ioc.typeread.impl

import org.tabooproject.reflex.ClassAnalyser
import org.tabooproject.reflex.Reflex.Companion.setProperty
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

class TypeReaderSingletonObject : TypeRead {

    override val type: Class<*> = Any::class.java

    override fun readAll(clazz: Class<*>, field: Field, database: IOCDatabase) {
        val genotype = field.genericType
        database.getDataAll().forEach { (t, u) ->
            if (u != null) {
                val target = Class.forName(genotype.typeName)
                println(genotype.typeName)
                val data = SerializationManager.deserialize(u, target, genotype)
                val assert = ClassAnalyser.analyse(clazz)
                val instance = clazz.getInstance()
                field.set(instance?.get(), data)
            }
        }
    }

    override fun writeAll(field: Field, source: Class<*>, database: IOCDatabase) {
        database.resetDatabase()
        val element = field.get(source)
        if (element != null) {
            database.saveData(TypeReadManager.getIndexId(element), element)
        }
        database.saveDatabase()
    }

    companion object {
        @Awake(LifeCycle.CONST)
        fun init() {
            val list = TypeReaderSingletonObject()
            TypeReadManager.typeReader[list.type.name] = list
        }
    }
}