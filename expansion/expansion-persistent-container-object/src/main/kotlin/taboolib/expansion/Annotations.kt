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
annotation class Length(val value: Int = 64)

@Retention(AnnotationRetention.RUNTIME)
annotation class TypeSQL(val value: ColumnTypeSQL)

@Retention(AnnotationRetention.RUNTIME)
annotation class TypeSQLite(val value: ColumnTypeSQLite)

@Retention(AnnotationRetention.RUNTIME)
annotation class Alias(val value: String)
