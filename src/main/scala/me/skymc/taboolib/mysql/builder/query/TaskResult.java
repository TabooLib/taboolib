package me.skymc.taboolib.mysql.builder.query;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author sky
 * @Since 2018-07-03 22:02
 */
public interface TaskResult {

    Object execute(ResultSet resultSet) throws SQLException;

}
