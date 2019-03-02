package me.skymc.taboolib.mysql;

import org.bukkit.plugin.Plugin;

/**
 * @Author sky
 * @Since 2018-05-14 19:07
 */
public abstract class IHost {

    private Plugin plugin;
    private boolean autoClose;

    public IHost(Plugin plugin) {
        this.plugin = plugin;
    }

    public IHost(Plugin plugin, boolean autoClose) {
        this.plugin = plugin;
        this.autoClose = autoClose;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    abstract public String getConnectionUrl();

    abstract public String getConnectionUrlSimple();
}
