package me.skymc.taboolib.mysql.builder.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author sky
 * @Since 2018-07-03 22:02
 */
public interface TaskStatement {

    void execute(PreparedStatement preparedStatement) throws SQLException;

}
