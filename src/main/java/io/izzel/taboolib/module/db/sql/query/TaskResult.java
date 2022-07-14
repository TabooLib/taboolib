package io.izzel.taboolib.module.db.sql.query;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author sky
 * @since 2018-07-03 22:02
 */
public interface TaskResult {

    Object execute(ResultSet resultSet) throws SQLException;

}
