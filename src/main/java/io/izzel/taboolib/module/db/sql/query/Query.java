package io.izzel.taboolib.module.db.sql.query;

import io.izzel.taboolib.module.db.sql.SQLTable;

public abstract class Query {

    protected SQLTable table;

    public Query(SQLTable table) {
        this.table = table;
    }

    public abstract String toQuery();
}
