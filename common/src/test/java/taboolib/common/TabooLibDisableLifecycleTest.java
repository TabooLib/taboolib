package taboolib.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabooLibDisableLifecycleTest {

    @AfterEach
    void restoreStoppedFlag() {
        TabooLib.setStopped(false);
    }

    @Test
    void disableLifecycleStillRunsWhenLoadingWasStopped() {
        AtomicInteger calls = new AtomicInteger();
        TabooLib.registerLifeCycleTask(LifeCycle.DISABLE, 0, calls::incrementAndGet);
        TabooLib.setStopped(true);

        TabooLib.lifeCycle(LifeCycle.DISABLE);

        assertEquals(1, calls.get());
        assertEquals(LifeCycle.DISABLE, TabooLib.getCurrentLifeCycle());
        assertTrue(TabooLib.isStopped());
    }
}
