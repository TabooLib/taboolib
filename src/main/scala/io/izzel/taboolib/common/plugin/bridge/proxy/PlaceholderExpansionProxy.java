package io.izzel.taboolib.common.plugin.bridge.proxy;

import io.izzel.taboolib.common.plugin.bridge.BridgeProxy;
import io.izzel.taboolib.module.compat.PlaceholderHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * TabooLib
 * io.izzel.taboolib.common.plugin.bridge.proxy.PlaceholderExpansionProxy
 *
 * @author sky
 * @since 2021/1/11 11:09 上午
 */
public class PlaceholderExpansionProxy extends PlaceholderExpansion implements BridgeProxy {

    private PlaceholderHook.Expansion expansion;

    @Override
    public void initProxy(String id, Object source) {
        if (id.equals("expansion")) {
            expansion = (PlaceholderHook.Expansion) source;
        }
    }

    @Override
    public String getIdentifier() {
        return expansion.identifier();
    }

    @Override
    public String getPlugin() {
        return expansion.plugin().getName();
    }

    @Override
    public String getAuthor() {
        return expansion.plugin().getName();
    }

    @Override
    public String getVersion() {
        return expansion.plugin().getName();
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {
        return expansion.onPlaceholderRequest(player, s);
    }
}
