package io.izzel.taboolib.module.db.sql;

import io.izzel.taboolib.module.db.IHost;
import io.izzel.taboolib.module.db.source.DBSource;
import io.izzel.taboolib.util.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-14 19:01
 */
public class SQLHost extends IHost {

    private final String host;
    private final String port;
    private final String user;
    private final String password;
    private final String database;
    private final List<String> flags = Arrays.asList("characterEncoding=utf-8", "useSSL=false");

    public SQLHost(ConfigurationSection section, Plugin plugin) {
        this(section, plugin, false);
    }

    public SQLHost(ConfigurationSection section, Plugin plugin, boolean autoClose) {
        this(section.getString("host", "localhost"), section.getString("port", "3306"), section.getString("user", "root"), section.getString("password", ""), section.getString("database", "test"), plugin);
    }

    public SQLHost(String host, String port, String user, String password, String database, Plugin plugin) {
        this(host, port, user, password, database, plugin, false);
    }

    public SQLHost(String host, String port, String user, String password, String database, Plugin plugin, boolean autoClose) {
        super(plugin, autoClose);
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    @Override
    public String getConnectionUrl() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}" + getFlagsInUrl(), this.host, this.port, this.database);
    }

    @Override
    public String getConnectionUrlSimple() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}", this.host, this.port, this.database);
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public List<String> getFlags() {
        return flags;
    }

    public String getFlagsInUrl() {
        if (flags.isEmpty()) {
            return "";
        }
        String collect = flags.stream().map(f -> f + "&").collect(Collectors.joining());
        return "?" + collect.substring(0, collect.length() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SQLHost)) {
            return false;
        }
        SQLHost sqlHost = (SQLHost) o;
        return Objects.equals(getHost(), sqlHost.getHost()) &&
                Objects.equals(getUser(), sqlHost.getUser()) &&
                Objects.equals(getPort(), sqlHost.getPort()) &&
                Objects.equals(getPassword(), sqlHost.getPassword()) &&
                Objects.equals(getDatabase(), sqlHost.getDatabase()) &&
                Objects.equals(getFlags(), sqlHost.getFlags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getUser(), getPort(), getPassword(), getDatabase(), getFlags());
    }

    @Override
    public String toString() {
        return "SQLHost{" +
                "host='" + host + '\'' +
                ", user='" + user + '\'' +
                ", port='" + port + '\'' +
                ", password='" + password + '\'' +
                ", database='" + database + '\'' +
                ", flags=" + flags +
                '}';
    }
}
