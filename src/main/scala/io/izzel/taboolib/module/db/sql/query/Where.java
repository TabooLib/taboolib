package io.izzel.taboolib.module.db.sql.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Where {

    private final String row;
    private String symbol;
    private Object value;
    private Object between;
    private Object[] in;

    Where(String row, String symbol, Object value) {
        this.row = row;
        this.symbol = symbol;
        this.value = value;
    }

    Where(String row, String symbol, Object value, Object between) {
        this.row = row;
        this.symbol = symbol;
        this.value = value;
        this.between = between;
    }

    Where(String row, Object[] in) {
        this.row = row;
        this.in = in;
    }

    public static Where is(String row, Object value) {
        return new Where(row, "=", value);
    }

    public static Where equals(String row, Object value) {
        return new Where(row, "=", value);
    }

    public static Where isNot(String row, Object value) {
        return new Where(row, "<>", value);
    }

    public static Where equalsNot(String row, Object value) {
        return new Where(row, "<>", value);
    }

    public static Where more(String row, Object value) {
        return new Where(row, ">", value);
    }

    public static Where less(String row, Object value) {
        return new Where(row, "<", value);
    }

    public static Where moreEqual(String row, Object value) {
        return new Where(row, ">=", value);
    }

    public static Where lessEqual(String row, Object value) {
        return new Where(row, "<=", value);
    }

    public static Where like(String row, Object value) {
        return new Where(row, "like", value);
    }

    public static Where between(String row, Object value1, Object value2) {
        return new Where(row, "between", value1, value2);
    }

    public static Where betweenNot(String row, Object value1, Object value2) {
        return new Where(row, "not between", value1, value2);
    }

    public static Where in(String row, Object... value3) {
        return new Where(row, value3);
    }

    public int toStatement(PreparedStatement statement, int index) throws SQLException {
        if (between == null) {
            setStatement(statement, index, value);
            return index + 1;
        } else if (in == null) {
            setStatement(statement, index, value);
            setStatement(statement, index + 1, between);
            return index + 2;
        } else {
            for (int i = 0; i < in.length; i++) {
                setStatement(statement, index + i, in[i]);
            }
            return index + in.length;
        }
    }

    public void setStatement(PreparedStatement statement, int index, Object obj) throws SQLException {
        if (obj instanceof Boolean) {
            statement.setBoolean(index, (boolean) obj);
        } else if (obj instanceof Integer) {
            statement.setInt(index, (int) obj);
        } else if (obj instanceof Double) {
            statement.setDouble(index, (double) obj);
        } else if (obj instanceof Long) {
            statement.setLong(index, (long) obj);
        } else if (obj instanceof Float) {
            statement.setFloat(index, (float) obj);
        } else if (obj instanceof Short) {
            statement.setShort(index, (short) obj);
        } else if (obj instanceof Byte) {
            statement.setByte(index, (byte) obj);
        } else if (obj instanceof byte[]) {
            statement.setBytes(index, (byte[]) obj);
        } else {
            statement.setObject(index, obj);
        }
    }

    public String toQuery() {
        if (in != null) {
            return "`" + row + "` in (" + Arrays.stream(in).map(i -> "?").collect(Collectors.joining(", ")) + ")";
        } else if (between != null) {
            return "`" + row + "` " + symbol + " ? and ?";
        } else {
            return "`" + row + "` " + symbol + " ?";
        }
    }
}