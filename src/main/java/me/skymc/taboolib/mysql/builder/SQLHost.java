package me.skymc.taboolib.mysql.builder;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private boolean autoClose;
    private List<String> flags = ArrayUtils.asList("characterEncoding=utf-8", "useSSL=false");

    public SQLHost(ConfigurationSection section, Plugin plugin) {
        this(section, plugin, false);
    }

    public SQLHost(ConfigurationSection section, Plugin plugin, boolean autoClose) {
        this(section.getString("host", "localhost"), section.getString("user", "root"), section.getString("port", "3306"), section.getString("password", ""), section.getString("database", "test"), plugin);
    }

    public SQLHost(String host, String user, String port, String password, String database, Plugin plugin) {
        this(host, user, port, password, database, plugin, false);
    }

    public SQLHost(String host, String user, String port, String password, String database, Plugin plugin, boolean autoClose) {
        this.host = host;
        this.user = user;
        this.port = port;
        this.password = password;
        this.database = database;
        this.plugin = plugin;
        this.autoClose = false;
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

    public boolean isAutoClose() {
        return autoClose;
    }

    public List<String> getFlags() {
        return flags;
    }

    public String getConnectionUrl() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}" + getFlagsInUrl(), this.host, this.port, this.database);
    }

    public String getConnectionUrlSimple() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}", this.host, this.port, this.database);
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
        return autoClose == sqlHost.autoClose &&
                Objects.equals(getHost(), sqlHost.getHost()) &&
                Objects.equals(getUser(), sqlHost.getUser()) &&
                Objects.equals(getPort(), sqlHost.getPort()) &&
                Objects.equals(getPassword(), sqlHost.getPassword()) &&
                Objects.equals(getDatabase(), sqlHost.getDatabase()) &&
                Objects.equals(getPlugin(), sqlHost.getPlugin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getUser(), getPort(), getPassword(), getDatabase(), getPlugin(), autoClose);
    }

    @Override
    public String toString() {
        return "SQLHost{" +
                "host='" + host + '\'' +
                ", user='" + user + '\'' +
                ", port='" + port + '\'' +
                ", password='" + password + '\'' +
                ", database='" + database + '\'' +
                ", plugin=" + plugin +
                ", autoClose=" + autoClose +
                '}';
    }
}
