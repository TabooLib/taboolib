package io.izzel.taboolib.test;

import io.izzel.taboolib.module.db.sql.*;
import io.izzel.taboolib.module.db.sql.query.Where;
import io.izzel.taboolib.util.Pair;

import javax.sql.DataSource;
import java.util.List;

public class SQL {

    private final SQLHost host;
    private final SQLTable table = SQLTable.create("test")
            .column(SQLColumnType.VARCHAR.toColumn(16, "uuid").columnOptions(SQLColumnOption.UNIQUE_KEY))
            .column(SQLColumnType.VARCHAR.toColumn(16, "name").columnOptions(SQLColumnOption.KEY))
            .column(SQLColumnType.VARCHAR.toColumn(16, "display").columnOptions(SQLColumnOption.KEY))
            .column(SQLColumnType.VARCHAR.toColumn(32, "value"))
            .column(SQLColumn.GMT_CREATE)
            .column(SQLColumn.GMT_MODIFIED);

    private DataSource dataSource;

    public SQL(SQLHost host) {
        this.host = host;
    }

    public void init() {
        try {
            table.create(dataSource = host.createDataSource());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public List<Pair<String, Double>> getValues() {
        return table.select().to(dataSource)
                .map(r -> Pair.of(r.getString("name"), r.getBigDecimal("value").doubleValue()));
    }

    public double getValue(String name) {
        return table.select(Where.equals("name", name))
                .limit(1)
                .to(dataSource)
                .firstOrElse(r -> r.getBigDecimal("value").doubleValue(), 0D);
    }

    public void updateValue(String name, double value) {
        table.update(Where.equals("name", name))
                .insertIfAbsent(null, name, name, name, value, null, null)
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
