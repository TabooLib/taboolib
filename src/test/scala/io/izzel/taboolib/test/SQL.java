package io.izzel.taboolib.test;

import io.izzel.taboolib.module.db.source.DBSource;
import io.izzel.taboolib.module.db.sql.*;
import io.izzel.taboolib.module.db.sql.query.Where;

import javax.sql.DataSource;

public class SQL {

    private final SQLHost host;
    private SQLTable table;
    private DataSource dataSource;

    public SQL(SQLHost host) {
        this.host = host;
    }

    public void init() {
        table = new SQLTable("test",
                SQLColumn.PRIMARY_KEY_ID,
                SQLColumnType.VARCHAR.toColumn(16, "name").columnOptions(SQLColumnOption.UNIQUE_KEY),
                SQLColumnType.BINARY.toColumn(10, 2, "value"));
        try {
            table.create(dataSource = DBSource.create(host));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public double getValue(String name) {
        return table.select(Where.equals("name", name))
                .limit(1)
                .to(dataSource)
                .resultNext(r -> r.getBigDecimal("value").doubleValue()).run(0D, Double.TYPE);
    }

    public void updateValue(String name, double value) {
        table.update(Where.equals("name", name))
                .insertIfAbsent(null, name, value)
                .set("value", value)
                .run(dataSource);
    }

    public SQLHost getHost() {
        return host;
    }

    public SQLTable getTable() {
        return table;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
