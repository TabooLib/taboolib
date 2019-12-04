package io.izzel.taboolib.module.ipc;

public interface TabooIpcClient {

    boolean connect(TabooIpcConfig config) throws Exception;

    void disconnect() throws Exception;

    boolean sendMessage(byte[] bytes, int index, int length, MessageBlock target);

    default boolean sendMessage(byte[] bytes, MessageBlock target) {
        return sendMessage(bytes, 0, bytes.length, target);
    }

    boolean readMessage(byte[] buf);

    int getId();

}
