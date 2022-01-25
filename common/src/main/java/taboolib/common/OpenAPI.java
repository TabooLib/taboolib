package taboolib.common;

import org.jetbrains.annotations.NotNull;
import taboolib.common.platform.PlatformFactory;

import java.util.Map;

/**
 * TabooLib
 * taboolib.common.OpenAPI
 *
 * @author sky
 * @since 2021/7/2 10:37 下午
 */
public class OpenAPI {

    OpenAPI() {
    }

    @NotNull
    public static OpenResult call(@NotNull String name, @NotNull Object[] data) {
        for (Object entry : PlatformFactory.INSTANCE.getInstances()) {
            if (entry instanceof OpenListener) {
                OpenResult result = ((OpenListener) entry).call(name, data);
                if (result.isSuccessful()) {
                    return result;
                }
            }
        }
        return OpenResult.failed();
    }
}
