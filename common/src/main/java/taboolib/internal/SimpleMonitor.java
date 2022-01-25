package taboolib.internal;

import taboolib.common.boot.Monitor;

/**
 * TabooLib
 * taboolib.internal.SimpleMonitor
 *
 * @author 坏黑
 * @since 2022/1/25 9:56 PM
 */
public class SimpleMonitor implements Monitor {

    boolean isShutdown = false;

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    public void setShutdown(boolean shutdown) {
        isShutdown = shutdown;
    }
}
