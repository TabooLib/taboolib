package io.izzel.taboolib.module.db.sqlite;

import io.izzel.taboolib.module.db.IHost;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Objects;

/**
 * SQLite 数据库地址
 *
 * @author 坏黑
 * @since 2018-12-08 12:58
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
