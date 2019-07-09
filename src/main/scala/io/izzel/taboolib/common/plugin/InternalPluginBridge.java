package io.izzel.taboolib.common.plugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.izzel.taboolib.module.lite.SimpleVersionControl;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-07-09 17:10
 */
public abstract class InternalPluginBridge {

    private static InternalPluginBridge handle;

    public static InternalPluginBridge handle() {
        return handle;
    }

    static {
        try {
            handle = (InternalPluginBridge) SimpleVersionControl.createNMS("io.izzel.taboolib.common.plugin.bridge.BridgeImpl").translateBridge().newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    abstract public String setPlaceholders(Player player, String args);

    abstract public List<String> setPlaceholders(Player player, List<String> args);

    abstract public Economy getEconomy();

    abstract public Permission getPermission();

    abstract public WorldGuard getWorldGuard();

    abstract public WorldGuardPlugin getWorldGuardPlugin();
}
