package taboolib.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassAppenderTest {

    @Test
    void initializesUnsafeAccessWithoutCompileTimeUnsafeDependency() {
        assertNotNull(ClassAppender.unsafe);
        assertNotNull(ClassAppender.lookup);
    }
}
