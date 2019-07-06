package io.izzel.taboolib.module.db.sql.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author sky
 * @Since 2018-07-03 22:02
 */
public interface TaskStatement {

    void execute(PreparedStatement preparedStatement) throws SQLException;

}
