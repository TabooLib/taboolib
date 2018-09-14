package me.skymc.taboolib.listener;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.mysql.MysqlUtils;
import me.skymc.taboolib.mysql.hikari.HikariHandler;
import me.skymc.taboolib.mysql.protect.MySQLConnection;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@TListener
public class ListenerPluginDisable implements Listener {

    @EventHandler
    public void disable(PluginDisableEvent e) {
        // 注销时间周期
        TimeCycleManager.cancel(e.getPlugin());
        // 注销数据库连接
        new HashSet<>(HikariHandler.getDataSource().keySet()).stream().filter(host -> e.getPlugin().equals(host.getPlugin()) && host.isAutoClose()).forEach(HikariHandler::closeDataSource);

        // 获取连接
        List<MySQLConnection> connection = new ArrayList<>();
        for (MySQLConnection conn : MysqlUtils.CONNECTIONS) {
            if (conn.getPlugin().equals(e.getPlugin())) {
                connection.add(conn);
                MysqlUtils.CONNECTIONS.remove(conn);
            }
        }

        // 异步注销
        BukkitRunnable runnable = new BukkitRunnable() {

            @Override
            public void run() {
                int i = 0;
                for (MySQLConnection conn : connection) {
                    conn.setFallReconnection(false);
                    conn.closeConnection();
                    i++;
                }
                if (i > 0) {
                    TLocale.Logger.info("MYSQL-CONNECTION.SUCCESS-CONNECTION-CANCEL", e.getPlugin().getName(), String.valueOf(i));
                }
            }
        };

        // 如果插件关闭
        try {
            runnable.runTaskLater(Main.getInst(), 40);
        } catch (Exception err) {
            TLocale.Logger.error("MYSQL-CONNECTION.FAIL-EXECUTE-TASK");
            runnable.run();
        }
    }
}
