package taboolib.common;

import taboolib.common.platform.PlatformFactory;

/**
 * TabooLib
 * taboolib.common.OpenAPI
 *
 * @author sky
 * @since 2021/7/2 10:37 下午
 */
public class OpenAPI {

    public static void register(String name, byte[] any, String[] args) {
        for (OpenReceiver receiver : PlatformFactory.INSTANCE.getOpenReceiver()) {
            if (receiver.register(name, any, args)) {
                return;
            }
        }
    }

    public static void unregister(String name, byte[] any, String[] args) {
        for (OpenReceiver receiver : PlatformFactory.INSTANCE.getOpenReceiver()) {
            if (receiver.unregister(name, any, args)) {
                return;
            }
        }
    }
}
