package me.skymc.taboolib.mysql.sqlite;

import me.skymc.taboolib.mysql.IHost;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Objects;

/**
 * @Author 坏黑
 * @Since 2018-12-08 12:58
 */
public class SQLiteHost extends IHost {

    private final File file;

    public SQLiteHost(File file, Plugin plugin) {
        super(plugin);
        this.file = file;
    }

    public SQLiteHost(File file, Plugin plugin, boolean autoClose) {
        super(plugin, autoClose);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getConnectionUrl() {
        return "jdbc:sqlite:" + file.getPath();
    }

    @Override
    public String getConnectionUrlSimple() {
        return "jdbc:sqlite:" + file.getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SQLiteHost)) {
            return false;
        }
        SQLiteHost that = (SQLiteHost) o;
        return Objects.equals(getFile(), that.getFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFile());
    }

    @Override
    public String toString() {
        return "SQLiteHost{" +
                "file=" + file +
                '}';
    }
}
