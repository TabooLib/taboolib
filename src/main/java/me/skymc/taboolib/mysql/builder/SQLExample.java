package me.skymc.taboolib.mysql.builder;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @Author sky
 * @Since 2018-07-02 23:43
 */
public class SQLExample extends JavaPlugin {

    private SQLHost sqlHost;
    private SQLTable sqlTable;
    private HikariDataSource dataSource;

    @Override
    public void onEnable() {
        int value = sqlTable.executeQuery("select * from table where username = ?")
                .dataSource(dataSource)
                .statement(statement -> statement.setString(1, "BlackSKY"))
                .resultNext(result -> result.getInt("value"))
                .run(0, 0);

        sqlTable.executeUpdate("statement table set value = ? where username = ?")
                .dataSource(dataSource)
                .statement(statement -> {
                    statement.setInt(1, 999);
                    statement.setString(2, "BlackSKY");
                }).run();
    }
}
