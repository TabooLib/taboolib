package io.izzel.taboolib.module.db.sql;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.IColumn;
import io.izzel.taboolib.util.Strings;

import java.util.Arrays;
import java.util.List;

/**
 * SQL 数据列
 *
 * @author sky
 * @since 2018-05-14 19:09
 */
public class SQLColumn extends IColumn {

    /**
     * ID 常量（id bigint unsigned not null auto_increment primary key）
     */
    public static final SQLColumn PRIMARY_KEY_ID = SQLColumnType.BIGINT.toColumn("id")
            .columnOptions(
                    SQLColumnOption.UNSIGNED,
                    SQLColumnOption.NOTNULL,
                    SQLColumnOption.AUTO_INCREMENT,
                    SQLColumnOption.PRIMARY_KEY
            );

    /**
     * GMT_CREATE 常量（gmt_create datetime not null default CURRENT_TIMESTAMP）
     */
    public static final SQLColumn GMT_CREATE = SQLColumnType.DATETIME.toColumn("gmt_create")
            .columnOptions(SQLColumnOption.NOTNULL)
            .defaultValue("$CURRENT_TIMESTAMP");

    /**
     * GMT_MODIFIED 常量（gmt_modified datetime not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP）
     */
    public static final SQLColumn GMT_MODIFIED = SQLColumnType.DATETIME.toColumn("gmt_modified")
            .columnOptions(SQLColumnOption.NOTNULL)
            .defaultValue("$CURRENT_TIMESTAMP")
            .update("CURRENT_TIMESTAMP");

    private final SQLColumnType columnType;
    private int m;
    private int d;

    private final String columnName;
    private Object defaultValue;
    private String update;
    private boolean descendingIndex;

    private final List<SQLColumnOption> columnOptions = Lists.newArrayList();

    public SQLColumn(SQLColumnType columnType, String columnName) {
        this(columnType, 0, 0, columnName, null);
    }

    public SQLColumn(SQLColumnType columnType, int m, String columnName) {
        this(columnType, m, 0, columnName, null);
    }

    public SQLColumn(SQLColumnType columnType, int m, int d, String columnName) {
        this(columnType, m, d, columnName, null);
    }

    public SQLColumn(SQLColumnType columnType, String columnName, SQLColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, null, columnOptions);
    }

    public SQLColumn(SQLColumnType columnType, String columnName, Object defaultValue) {
        this(columnType, 0, 0, columnName, defaultValue);
    }

    public SQLColumn(SQLColumnType columnType, String columnName, Object defaultValue, SQLColumnOption... columnOptions) {
        this(columnType, 0, 0, columnName, defaultValue, columnOptions);
    }

    public SQLColumn(SQLColumnType columnType, int m, int d, String columnName, Object defaultValue, SQLColumnOption... columnOptions) {
        this.columnType = columnType;
        this.m = m;
        this.d = d;
        this.columnName = columnName;
        this.defaultValue = defaultValue;
        this.columnOptions.addAll(Arrays.asList(columnOptions));
    }

    public SQLColumn m(int m) {
        this.m = m;
        return this;
    }

    public SQLColumn d(int d) {
        this.d = d;
        return this;
    }

    /**
     * 为该列赋予一项默认值
     * 如果参数含有单引号则需要添加转义符例如 "\\'"
     * <p>
     * 非字符串的特殊类型需在参数前添加 "$" 符号
     *
     * @param defaultValue 默认值
     * @return {@link SQLTable}
     */
    public SQLColumn defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public SQLColumn columnOptions(SQLColumnOption... columnOptions) {
        this.columnOptions.addAll(Arrays.asList(columnOptions));
        return this;
    }

    /**
     * 5.41 update
     *
     * @param update 更新行为
     * @return {@link SQLColumn}
     */
    public SQLColumn update(String update) {
        this.update = update;
        return this;
    }

    /**
     * 5.41 update
     * 倒序索引，在 SQL 8.0 中有效，在此之前可以使用，但不会生效。
     *
     * @return {@link SQLColumn}
     */
    public SQLColumn descendingIndex() {
        this.descendingIndex = true;
        return this;
    }

    @Override
    public String convertToCommand() {
        if (this.m == 0 && this.d == 0) {
            return Strings.replaceWithOrder("`{0}` {1} {2}", columnName, columnType.name().toLowerCase(), convertToOptions());
        } else if (this.d == 0) {
            return Strings.replaceWithOrder("`{0}` {1}({2}) {3}", columnName, columnType.name().toLowerCase(), m, convertToOptions());
        } else {
            return Strings.replaceWithOrder("`{0}` {1}({2},{3}) {4}", columnName, columnType.name().toLowerCase(), m, d, convertToOptions());
        }
    }

    private String convertToOptions() {
        StringBuilder builder = new StringBuilder();
        for (SQLColumnOption option : columnOptions) {
            if (option != SQLColumnOption.UNIQUE_KEY && option != SQLColumnOption.KEY) {
                builder.append(" ").append(option.getText());
            }
        }
        if (defaultValue instanceof String) {
            if (defaultValue.toString().startsWith("$")) {
                builder.append(" default ").append(defaultValue.toString().substring(1));
            } else {
                builder.append(" default '").append(defaultValue).append("'");
            }
        } else if (defaultValue != null) {
            builder.append(" default ").append(defaultValue);
        }
        if (update != null) {
            builder.append(" on update ").append(update);
        }
        return builder.toString().trim();
    }

    @Override
    public String toString() {
        return "SQLColumn{" +
                "columnType=" + columnType +
                ", m=" + m +
                ", d=" + d +
                ", columnName='" + columnName + '\'' +
                ", defaultValue=" + defaultValue +
                ", update='" + update + '\'' +
                ", descendingIndex=" + descendingIndex +
                ", columnOptions=" + columnOptions +
                '}';
    }

    public SQLColumnType getColumnType() {
        return columnType;
    }

    public int getM() {
        return m;
    }

    public int getD() {
        return d;
    }

    public String getColumnName() {
        return columnName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getUpdate() {
        return update;
    }

    public boolean isDescendingIndex() {
        return descendingIndex;
    }

    public List<SQLColumnOption> getColumnOptions() {
        return columnOptions;
    }
}
