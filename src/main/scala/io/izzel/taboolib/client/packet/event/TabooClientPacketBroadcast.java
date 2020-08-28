package io.izzel.taboolib.client.packet.event;

import io.izzel.taboolib.module.event.EventNormal;

/**
 * @Author sky
 * @Since 2020-08-29 0:10
 */
public class TabooClientPacketBroadcast extends EventNormal<TabooClientPacketBroadcast> {

    private final String value;

    public TabooClientPacketBroadcast(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
