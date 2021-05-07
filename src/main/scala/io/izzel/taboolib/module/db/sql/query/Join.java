package io.izzel.taboolib.module.db.sql.query;

import io.izzel.taboolib.util.Strings;

import java.util.List;
import java.util.StringJoiner;

/**
 * io.izzel.taboolib.module.db.sql.query.Join
 *
 * @author sky
 * @since 2021/4/16 9:07 上午
 */
public class Join {

    private final JoinType type;
    private final String from;
    private final List<JoinWhere> where;

    public Join(JoinType type, String from, List<JoinWhere> where) {
        this.type = type;
        this.from = from;
        this.where = where;
    }

    public JoinType getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public List<JoinWhere> getWhere() {
        return where;
    }

    public String toQuery() {
        if (where.isEmpty()) {
            return Strings.replaceWithOrder("{0} join `{1}`", type.toKey(), from);
        } else {
            StringJoiner joiner = new StringJoiner(" and ");
            for (JoinWhere where : where) {
                joiner.add(Strings.replaceWithOrder("`{0}`.`{1}` {2} `{3}`.`{4}`",
                        where.getLeftTable(),
                        where.getLeftField(),
                        where.getSymbol(),
                        where.getRightTable(),
                        where.getRightField()
                ));
            }
            return Strings.replaceWithOrder("{0} join `{1}` on {2}", type.toKey(), from, joiner.toString());
        }
    }
}
