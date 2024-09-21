package taboolib.common;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
        // PrimitiveIO.debug("OpenAPI call {0}", name);
        for (Map.Entry<String, Object> entry : TabooLib.getAwakenedClasses().entrySet()) {
            if (entry.getValue() instanceof OpenListener) {
                OpenResult result = ((OpenListener) entry.getValue()).call(name, data);
                if (result.isSuccessful()) {
                    // PrimitiveIO.debug("  Handle by {0}", entry.getKey());
                    return result;
                }
            }
        }
        return OpenResult.failed();
    }
}
