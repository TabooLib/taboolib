package io.izzel.taboolib.origin.client.packet;

import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
public abstract class Packet {

    private final int port;
    private final String uid;

    public Packet(int port) {
        this.port = port;
        this.uid = UUID.randomUUID().toString();
    }

    public int getPort() {
        return port;
    }

    public String getUid() {
        return uid;
    }

    public void readOnServer() {

    }

    public void readOnClient() {

    }

    public void serialize(JsonObject json) {

    }

    public void unSerialize(JsonObject json) {

    }

}
