package taboolib.module.porticus.common;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author 坏黑
 * @since 2018-04-16
 */
public class ByteUtils {

    public static String serialize(String var) {
        return Base64.getEncoder().encodeToString(var.getBytes(StandardCharsets.UTF_8));
    }

    public static String deSerialize(String var) {
        byte[] decoded = Base64.getDecoder().decode(var);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(decoded))
                    .toString();
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException("Serialized value is not valid UTF-8", ex);
        }
    }

    public static String[] serialize(String... var) {
        String[] varEncode = new String[var.length];
        for (int i = 0; i < var.length; i++) {
            varEncode[i] = Base64.getEncoder().encodeToString(var[i].getBytes(StandardCharsets.UTF_8));
        }
        return varEncode;
    }

    public static String[] deSerialize(String... var) {
        String[] varEncode = new String[var.length];
        for (int i = 0; i < var.length; i++) {
            varEncode[i] = deSerialize(var[i]);
        }
        return varEncode;
    }
}
