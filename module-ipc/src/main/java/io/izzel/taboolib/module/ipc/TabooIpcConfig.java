package io.izzel.taboolib.module.ipc;

public interface TabooIpcConfig {

    default long memorySize() {
        return 1 << 24;
    }

    default long blockSize() {
        return 1 << 14;
    }

    default long period() {
        return 10;
    }

    default long timeout() {
        return 60 * 1000;
    }

    String fileLocation();
}
