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

    OpenAPI() {
    }

    @NotNull
    public static OpenResult call(@NotNull String name, @NotNull Object[] data) {
        OpenResult result = PlatformFactory.INSTANCE.getAwakeInstances().stream()
                .filter(entry -> entry instanceof OpenListener)
                .map(entry -> (OpenListener) entry)
                .map(entry -> entry.call(name, data))
                .filter(OpenResult::isSuccessful)
                .findFirst()
                .orElse(null);

        if (result != null) { return result; }

        return OpenResult.failed();
    }
}
