package io.izzel.taboolib.module.db.sql.query;

/**
 * io.izzel.taboolib.module.db.sql.query.JoinType
 *
 * @author sky
 * @since 2021/4/16 9:08 上午
 */
public enum JoinType {

    LEFT, RIGHT, INNER;

    public String toKey() {
        return name().toLowerCase();
    }
}
