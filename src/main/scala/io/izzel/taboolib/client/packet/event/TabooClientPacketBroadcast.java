package io.izzel.taboolib.client.packet.event;

import io.izzel.taboolib.module.event.EventNormal;
import org.bukkit.Bukkit;

/**
 * @author sky
 * @since 2020-08-29 0:10
 */
public class TabooClientPacketBroadcast extends EventNormal<TabooClientPacketBroadcast> {

    private final String value;

    public TabooClientPacketBroadcast(String value) {
        this.value = value;
        async(!Bukkit.isPrimaryThread());
    }

    public String getValue() {
        return value;
    }
}
