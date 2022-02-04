package taboolib.module.database

/**
 * SQL 数据类型
 *
 * @author sky
 * @since 2018-05-14 19:13 (recode 2020/10/06 10:00)
 */
enum class ColumnTypeSQL(val isRequired: Boolean = false) {

    /**
     * TINYINT[(M)][UNSIGNED][ZEROFILL]
     *
     *
     * 空间: 1 字节
     * 有符号值: -128 to 127
     * 无符号值: 0 to 255
     *
     *
     * 最短的整数类型，M 为不影响值本身的可选的显示范围。
     */
    TINYINT,

    /**
     * SMALLINT[(M)][UNSIGNED][ZEROFILL]
     *
     *
     * 空间: 2 字节
     * 有符号值: -32,768 to 32,767
     * 无符号值: 0 to 65,535
     *
     *
     * M 为不影响值本身的可选的显示范围。
     */
    SMALLINT,

    /**
     * MEDIUMINT[(M)][UNSIGNED][ZEROFILL]
     *
     *
     * 空间: 3 字节
     * 有符号值: -8,388,608 to 8,388,607
     * 无符号值: 0 to 16,777,215
     *
     *
     * M 为不影响值本身的可选的显示范围。
     */
    MEDIUMINT,

    /**
     * INT[(M)][UNSIGNED][ZEROFILL]
     *
     *
     * 空间: 4 字节
     * 有符号值: -2,147,483,648 to 2,147,483,647
     * 无符号值: 0 to 4,294,967,295
     *
     *
     * M 为不影响值本身的可选的显示范围。
     */
    INT,

    /**
     * BIGINT[(M)][UNSIGNED][ZEROFILL]
     *
     *
     * 空间: 8 字节
     * 有符号值：-9,223,372,036,854,775,808 to 9,223,373,036,854,775,807
     * 无符号值：0 to 18,446,744,073,709,551,615
     *
     *
     * M 为不影响值本身的可选的显示范围。
     *
     *
     * 注意：较大值的算数运算可能会失败。
     */
    BIGINT,

    /**
     * FLOAT[(M,D)][UNSIGNED][ZEROFILL]
     *
     *
     * 精确到小数点后 7 位，IEEE 754 单精度浮点值。
     *
     *
     * M is the maximum number of digits, of witch D may be after the decimal point.
     *
     *
     * Note: Many decimal numbers can only be approximated by floating-point values.
     * See DECIMAL if you require exact results.
     */
    FLOAT,

    /**
     * DOUBLE[(M,D)][UNSIGNED][ZEROFILL]
     *
     *
     * 精确到小数点后 15 位，IEEE 754 双精度浮点值。
     *
     *
     * M is the maximum number of digits, of witch D may be after the decimal point.
     *
     *
     * Note: Many decimal numbers can only be approximated by floating-point values.
     * See DECIMAL if you require exact results.
     */
    DOUBLE,

    /**
     * DECIMAL[(M[,D])][UNSIGNED][ZEROFILL]
     *
     *
     * M (precision): Up to 65 digits
     * D (scala): 0 to 30 digits
     *
     *
     * A fixed-point, exact decimal value.
     * M is the maximum number of digits, of witch D may be after the decimal point.
     * When rounding, 0-4 is always rounded down, 5-9 up ("round to wards nearest").
     */
    DECIMAL,

    /**
     * BIT[(M)]
     * M: 1 (default) to 64
     *
     *
     * A bit field type.
     * M specifies the number of bits.
     * If shorter values are inserted, they will be aligned on the least significant bit.
     */
    BIT,

    /**
     * SERIAL
     * 别名类型，指向: BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE
     */
    SERIAL,

    /**
     * BOOL
     * 别名类型，指向: TINYINT(1)
     */
    BOOL,

    /**
     * BOOLEAN
     * 别名类型，指向: TINYINT(1)
     */
    BOOLEAN,

    /**
     * FIXED[(M[,D])][UNSIGNED][ZEROFILL]
     * 别名类型，指向: DECIMAL
     */
    FIXED,

    /**
     * CHAR(M)
     * M: 0 to 255 字符
     *
     *
     * A character string that will require Mxw bytes per row, independent of the actual content length.
     * w is the maximum number of bytes a single character can occupy in the given encoding.
     */
    CHAR(true),

    /**
     * VARCHAR(M)
     * M: 0 to 65,535 字符
     *
     *
     * A character string that can store up to M bytes, but requires less space for shorter values.
     * The actual number of characters is further limited by the used encoding and the values of other fields in the row.
     */
    VARCHAR(true),

    /**
     * TINYTEXT
     * Up to 255 字符
     *
     *
     * A character string that can store up to 255 bytes, but requires less space for shorter values.
     * The actual number of characters if further limited by the used encoding.
     * v
     */
    TINYTEXT,

    /**
     * TEXT[(M)]
     * Up to 65,535 字符
     *
     *
     * A character string that can store up to M bytes, but requires less space for shorter values.
     * The actual number of characters if further limited by the used encoding.
     * Unlike VARCHAR this type does not count towards the maximum row length.
     */
    TEXT,

    /**
     * MEDIUMTEXT
     * Up to 16,777,215 字符 (16 MiB)
     *
     *
     * 可变长度的字符串。
     * The actual number of characters if further limited by the used encoding.
     * Unlike VARCHAR this type does not count towards the maximum row length.
     */
    MEDIUMTEXT,

    /**
     * LONGTEXT
     * Up to 4,294,967,295 字符 (4 GiB)
     *
     *
     * 可变长度的字符串。
     * The actual number of characters if further limited by the used encoding.
     * Unlike VARCHAR this type does not count towards the maximum row length.
     */
    LONGTEXT,

    /**
     * TINYBLOB
     * Up to 255 bytes
     *
     *
     * 可变长度的字节数组。
     * Unlike VARCHAR this type does not count towards the maximum row length.
     */
    TINYBLOB,

    /**
     * MEDIUMBLOB
     * Up to 16,777,215 bytes (16 MiB)
     *
     *
     * 可变长度的字节数组。
     * Unlike VARCHAR this type does not count towards the maximum row length.
     */
    MEDIUMBLOB,

    /**
     * BLOB
     * Up to 65,535 bytes
     *
     *
     * 可变长度的字节数组。
     * Unlike VARCHAR this type does not count towards the maximum row length.
     */
    BLOB,

    /**
     * LONGBLOB
     * Up to 4,294,967,295 字符 (4 GiB)
     *
     *
     * 可变长度的字节数组。
     * Unlike VARCHAR this type does not count towards the maximum row length.
     */
    LONGBLOB,

    /**
     * BINARY(M)
     * M: 0 to 255 字节
     *
     *
     * 定长的字节数组。
     * Shorter values will always be padded to the right with 0x00 unit they fit M.
     */
    BINARY(true),

    /**
     * VARBINARY(M)
     * M: 0 to 65,635 字节
     *
     *
     * 可变长度的字节数组。
     * The actual number of bytes is further limited by the values of other fields in the row.
     */
    VARBINARY(true),

    /**
     * JSON
     * Limited to @@max_allowed_packet
     *
     *
     * A data type that validates JSON data in INSERT and internally stores is in a binary format that is both, more compat and father to access than textual JSON.
     * Available from MySQL 5.7.8
     */
    JSON,

    /**
     * ENUM('member',...)
     * up to 65,535 distinct members (less 3,000 in practice)
     * 1-2 bytes storage
     *
     *
     * Defines a list of members, of witch every field can use at most one.
     * Values are sorted by their index number (string at 0 for the first member).
     */
    ENUM,

    /**
     * SET('member',...)
     * Range 1 to 64 members
     * 1, 2, 3, 4 or 8 bytes storage
     *
     *
     * A SET can define up to 64 members (as strings) of witch a field can use one or more using a comma-separated list.
     * Upon insertion the order of members is automatically normalized and duplicate members will be eliminated.
     * Assignment of numbers is supported using the same semantics as for BIT types.
     */
    SET,

    /**
     * DATE
     * Range: 1000-01-01 to 9999-12-31
     *
     *
     * Stores a date without time information.
     * The representation is YYYY-MM-DD.
     * The value is not affected by any time zone setting.
     * Invalid value are converted to 0000-00-00.
     */
    DATE,

    /**
     * DATETIME[(F)]
     * Range: 1000-01-01 00:00:00.0 to 9999-12-31 23:59:59:999999
     * F (precision): 0 (1s) to 1 (1µs)
     *
     *
     * Stores a date and time of day.
     * The representation is YYYY-MM-DD HH:MM:SS[.|*], I being fractional seconds.
     * The value is not affected by any time zone setting.
     * Invalid are converted to 0000-00-00 00:00:00.0.
     * Fractional seconds were added in MySQL 5.6.4 with a precision down to microseconds (6), specified by F.
     */
    DATETIME,

    /**
     * TIMESTAMP[(F)]
     * Range: 1970-01-01 00:00:01.0 to 2038 01-19 03:14:07:999999
     * F (precision): 0 (1s) to 6 (1µs)
     *
     *
     * Stores a date and time of day as seconds since the beginning of the UNIX epoch (1970-01-01 00:00:00).
     * The values displayed/store are affected be the connection's @@time_zone settings.
     * The representation is the same as for DATETIME.
     * Invalid value, as will "second zero", are converted to 0000-00-00 00:00:00.0.
     * Fractional seconds ware added in MySQL 5.6.4 with a precision down to microseconds (6), specified by F.
     * Some additional rules may apply.
     */
    TIMESTAMP,

    /**
     * TIME[(F)]
     * Range: -838:59:59.0 to 838:59:59.0
     * F (precision): 0 (1s) to 6 (1µs)
     *
     *
     * Stores a time of day, duration of time interval.
     * The representation is HH:MM:SS[.|*], being fractional seconds.
     * The value is not affected by any time zone setting.
     * Invalid values are converted to 00:00:00.
     * Fractional seconds ware added in MySQL 5.6.4 with a precision down to microseconds (6), specified by F.
     */
    TIME,

    /**
     * YEAR(4)
     * Range: 0000, 1901 to 2155
     *
     *
     * Represents a 4 digit year value, stored as 1 byte.
     * Invalid values are converted to 0000 and two digit values 0 to 69 will be converted to years 2000 to 2069, resp.
     * values 70 to 99 to years 1970 to 1999.
     * The YEAR(2) type wars removed in MySQL 5.7.5
     */
    YEAR,

    /**
     * GEOMETRY
     *
     *
     * Can store a single spatial value of types POINT, LINESTRING or POLYGON Spatial support in MySQL is based on the OpenGIS Geometry Model.
     */
    GEOMETRY,

    /**
     * POINT
     *
     *
     * Represents a single location in coordinate space using X and Y coordinates.
     * The point is zero-dimensional.
     */
    POINT,

    /**
     * LINESTRING
     *
     *
     * Represents an ordered set of coordinates where each consecutive pair of two piints is connected by a straight line.
     */
    LINESTRING,

    /**
     * POLYGON
     *
     *
     * Creates a surface by combining one LinearRing (ie. a LineString that is closed and simple) as the outside boundary with zero of more inner LinearRings acting as "holes".
     */
    POLYGON,

    /**
     * MULTIPOINT
     *
     *
     * Represents a set of Points without specifying any kind of relation and/or order between them.
     */
    MULTIPOINT,

    /**
     * MULTILINESTRING
     *
     *
     * Represents a collection of LineStrings.
     */
    MULTILINESTRING,

    /**
     * MULTIPOLYGON
     *
     *
     * Represents a collection of Polygons.
     * The Polygons making up the MultiPolygon must not intersect.
     */
    MULTIPOLYGON,

    /**
     * Represents a collection of objects of any other single- or multi-valued spatial type.
     * The only restriction being, that all objects must share a common coordinate system.
     */
    GEOMETRYCOLLECTION;

    operator fun invoke(name: String, parameter1: Int = 0, parameter2: Int = 0, func: ColumnSQL.() -> Unit = {}): ColumnSQL {
        return ColumnSQL(this, name).also {
            it.parameter[0] = parameter1
            it.parameter[1] = parameter2
            func(it)
        }
    }
}