package taboolib.library.kether;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleReaderTest {

    @Test
    public void nextTokenBlockShouldReadEmptyQuotedString() {
        BlockReader blockReader = new BlockReader("\"\"".toCharArray(), null, Collections.emptyList());
        SimpleReader reader = new SimpleReader(null, blockReader, Collections.emptyList());

        TokenBlock tokenBlock = reader.nextTokenBlock();

        assertEquals("", tokenBlock.getToken());
        assertTrue(tokenBlock.isBlock());
        assertEquals(2, reader.getIndex());
    }
}
