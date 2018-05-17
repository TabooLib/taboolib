package me.skymc.taboolib.mysql.builder;

import com.ilummc.tlib.util.Ref;
import com.ilummc.tlib.util.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * @Author sky
 * @Since 2018-05-14 19:01
 */
public class SQLHost {

    private String host;
    private String user;
    private String port;
    private String password;
    private String database;
    private Plugin plugin;

    public SQLHost(ConfigurationSection section, Plugin plugin) {
        this(section.getString("host", "localhost"), section.getString("user", "root"), section.getString("port", "3306"), section.getString("password", ""), section.getString("database", "test"), plugin);
    }

    public SQLHost(String host, String user, String port, String password, String database, Plugin plugin) {
        this.host = host;
        this.user = user;
        this.port = port;
        this.password = password;
        this.database = database;
        this.plugin = plugin;
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

    public Plugin getPlugin() {
        return plugin;
    }

    public String getConnectionUrl() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}?characterEncoding=utf-8&useSSL=false", this.host, this.port, this.database);
    }

    public String getConnectionUrlSimple() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}", this.host, this.port, this.database);
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
        return Objects.equals(getHost(), sqlHost.getHost()) && Objects.equals(getPort(), sqlHost.getPort()) && Objects.equals(getUser(), sqlHost.getUser()) && Objects.equals(getPassword(), sqlHost.getPassword()) && Objects.equals(getDatabase(), sqlHost.getDatabase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getUser(), getPort(), getPassword(), getDatabase());
    }

    @Override
    public String toString() {
        return "host='" + "SQLHost{" + host + '\'' + ", user='" + user + '\'' + ", port='" + port + '\'' + ", password='" + password + '\'' + ", database='" + database + '\'' + '}';
    }
}
