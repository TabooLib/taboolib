package taboolib.module.database

abstract class ColumnBuilder {

    internal var name: String? = null

    fun name(name: String) {
        this.name = name
    }

    abstract fun getColumn(): Column
}

class SQL : ColumnBuilder() {

    internal var type: ColumnTypeSQL? = null
    internal var extra: (ColumnSQL.() -> Unit)? = null
    internal val parameter = arrayOf(0, 0)

    fun type(type: ColumnTypeSQL, parameter1: Int = 0, parameter2: Int = 0, extra: ColumnSQL.() -> Unit = {}) {
        this.type = type
        this.parameter[0] = parameter1
        this.parameter[1] = parameter2
        this.extra = extra
    }

    fun extra(extra: ColumnSQL.() -> Unit) {
        this.extra = extra
    }

    /**
     * ID 常量（id bigint unsigned not null auto_increment primary key）
     */
    fun id() {
        name = "id"
        type = ColumnTypeSQL.BIGINT
        extra {
            options = arrayOf(ColumnOptionSQL.UNSIGNED, ColumnOptionSQL.NOTNULL, ColumnOptionSQL.AUTO_INCREMENT, ColumnOptionSQL.PRIMARY_KEY)
        }
    }

    /**
     * GMT_CREATE 常量（gmt_create datetime not null default CURRENT_TIMESTAMP）
     */
    fun timeCreate() {
        name = "gmt_create"
        type = ColumnTypeSQL.DATETIME
        extra {
            options = arrayOf(ColumnOptionSQL.NOTNULL)
            def = "\$CURRENT_TIMESTAMP"
        }
    }

    /**
     * GMT_MODIFIED 常量（gmt_modified datetime not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP）
     */
    fun timeModified() {
        name = "gmt_modified"
        type = ColumnTypeSQL.DATETIME
        extra {
            options = arrayOf(ColumnOptionSQL.NOTNULL)
            def = "\$CURRENT_TIMESTAMP"
            onUpdate = "CURRENT_TIMESTAMP"
        }
    }

    override fun getColumn(): Column {
        val column = type!!(name!!)
        column.parameter(parameter[0], parameter[1])
        extra?.invoke(column)
        return column
    }
}

class SQLite : ColumnBuilder() {

    internal var type: ColumnTypeSQLite? = null
    internal var extra: (ColumnSQLite.() -> Unit)? = null
    internal val parameter = arrayOf(0, 0)

    fun type(type: ColumnTypeSQLite, parameter1: Int = 0, parameter2: Int = 0, extra: ColumnSQLite.() -> Unit = {}) {
        this.type = type
        this.parameter[0] = parameter1
        this.parameter[1] = parameter2
        this.extra = extra
    }

    fun extra(func: ColumnSQLite.() -> Unit) {
        extra = func
    }

    /**
     * ID 常量（id integer not null primary key）
     */
    fun id() {
        name = "id"
        type = ColumnTypeSQLite.INTEGER
        extra {
            options = arrayOf(ColumnOptionSQLite.NOTNULL, ColumnOptionSQLite.PRIMARY_KEY)
        }
    }

    override fun getColumn(): Column {
        val column = type!!(name!!)
        column.parameter(parameter[0], parameter[1])
        extra?.invoke(column)
        return column
    }
}