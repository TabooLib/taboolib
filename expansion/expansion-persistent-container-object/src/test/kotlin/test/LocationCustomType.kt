package test

import org.bukkit.Bukkit
import org.bukkit.Location
import taboolib.expansion.CustomType
import taboolib.expansion.CustomTypeData
import taboolib.module.database.ColumnOptionSQLite
import taboolib.module.database.ColumnTypeSQL

@CustomType
object LocationCustomType : CustomTypeData {

    override val sqlLiteType: ColumnOptionSQLite = ColumnOptionSQLite.NOTNULL

    override val sqlType: ColumnTypeSQL = ColumnTypeSQL.VARCHAR

    override fun deserialize(obj: Any): Any {
        val local = obj as String
        val split = local.split("__")
        val world = split.getOrNull(0)
        val x = split[1].toDoubleOrNull() ?: 0.0
        val y = split[2].toDoubleOrNull() ?: 0.0
        val z = split[3].toDoubleOrNull() ?: 0.0
        val yaw = split[4].toFloatOrNull() ?: 0F
        val pitch = split[5].toFloatOrNull() ?: 0F
        return Location(world?.let { Bukkit.getWorld(it) }, x, y, z, yaw, pitch)
    }

    override fun serialize(obj: Any): Any {
        val location = obj as Location
        return "${location.world?.name}__${location.x}__${location.y}__${location.z}__${location.yaw}__${location.pitch}"
    }

    override fun isThis(obj: Any): Boolean {
        return obj is Location
    }

    override fun isThisByClass(clazz: Class<*>): Boolean {
        return clazz == Location::class.java
    }
}
