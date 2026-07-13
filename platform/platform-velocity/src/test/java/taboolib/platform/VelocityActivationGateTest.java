package taboolib.platform;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelocityActivationGateTest {

    @Test
    void shutdownClosesGateBeforeLateActivation() {
        VelocityActivationGate gate = new VelocityActivationGate();
        AtomicInteger activeCalls = new AtomicInteger();

        CompletableFuture<Void> closed = gate.close();

        assertTrue(closed.isDone());
        assertFalse(gate.activate(activeCalls::incrementAndGet));
        assertEquals(0, activeCalls.get());
    }

    @Test
    void disableContinuationWaitsForClaimedActivation() {
        VelocityActivationGate gate = new VelocityActivationGate();
        List<String> order = new ArrayList<>();
        AtomicReference<CompletableFuture<Void>> closed = new AtomicReference<>();

        assertTrue(gate.activate(() -> {
            order.add("active-start");
            closed.set(gate.close());
            assertFalse(closed.get().isDone());
            closed.get().thenRun(() -> order.add("disable"));
            order.add("active-end");
        }));

        assertTrue(closed.get().isDone());
        assertEquals(Arrays.asList("active-start", "active-end", "disable"), order);
    }

    @Test
    void activationCanOnlyBeClaimedOnce() {
        VelocityActivationGate gate = new VelocityActivationGate();
        AtomicInteger activeCalls = new AtomicInteger();

        assertTrue(gate.activate(activeCalls::incrementAndGet));
        assertFalse(gate.activate(activeCalls::incrementAndGet));
        assertTrue(gate.close().isDone());
        assertEquals(1, activeCalls.get());
    }

    @Test
    void shutdownKeepsLegacyDescriptorAndUsesAsyncEventContract() throws NoSuchMethodException {
        java.lang.reflect.Method legacy = VelocityPlugin.class.getDeclaredMethod("e", ProxyShutdownEvent.class);
        java.lang.reflect.Method async = VelocityPlugin.class.getDeclaredMethod("eAsync", ProxyShutdownEvent.class);

        assertEquals(void.class, legacy.getReturnType());
        assertNull(legacy.getAnnotation(Subscribe.class));
        assertEquals(EventTask.class, async.getReturnType());
        assertTrue(async.isAnnotationPresent(Subscribe.class));
    }
}
