package taboolib.expansion

import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.ColumnTypeSQLite

@Retention(AnnotationRetention.RUNTIME)
annotation class Id

@Retention(AnnotationRetention.RUNTIME)
annotation class Key

@Retention(AnnotationRetention.RUNTIME)
annotation class UniqueKey

@Retention(AnnotationRetention.RUNTIME)
annotation class Length(val value: Int = 64, val long: Boolean = false, val medium: Boolean = false,val char: Boolean = false)

@Retention(AnnotationRetention.RUNTIME)
annotation class Alias(val value: String)
