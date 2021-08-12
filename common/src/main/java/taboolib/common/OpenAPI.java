package taboolib.common;

import org.jetbrains.annotations.NotNull;
import taboolib.common.platform.PlatformFactory;

/**
 * TabooLib
 * taboolib.common.OpenAPI
 *
 * @author sky
 * @since 2021/7/2 10:37 下午
 */
public class OpenAPI {

    @NotNull
    public static OpenResult call(String name, Object[] data) {
        for (OpenListener receiver : PlatformFactory.INSTANCE.getOpenListener()) {
            OpenResult result = receiver.call(name, data);
            if (result.isSuccessful()) {
                return result;
            }
        }
        return OpenResult.failed();
    }
}
