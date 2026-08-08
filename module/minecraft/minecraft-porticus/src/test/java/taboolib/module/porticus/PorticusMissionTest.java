package taboolib.module.porticus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PorticusMissionTest {

    @AfterEach
    void clearMissions() {
        Porticus.INSTANCE.getMissions().clear();
    }

    @Test
    void unstartedMissionNeverTimesOut() {
        TestMission mission = new TestMission();
        mission.now = Long.MAX_VALUE;
        mission.timeout(0, TimeUnit.MILLISECONDS);

        assertFalse(mission.isTimeout());
    }

    @Test
    void timeoutUsesElapsedTimeAndIncludesBoundary() {
        TestMission mission = new TestMission();
        mission.now = 1_000;
        mission.timeout(100, TimeUnit.MILLISECONDS);
        mission.run(new Object());

        mission.now = 1_099;
        assertFalse(mission.isTimeout());
        mission.now = 1_100;
        assertTrue(mission.isTimeout());
    }

    @Test
    void pendingMissionCannotBeStartedAgain() {
        TestMission mission = new TestMission();
        mission.now = 1_000;
        mission.onTimeout(() -> {
        });
        mission.run(new Object());

        mission.now = 2_000;

        assertThrows(IllegalStateException.class, () -> mission.run(new Object()));
        assertEquals(1, Porticus.INSTANCE.getMissions().size());
        assertSame(mission, Porticus.INSTANCE.getMissions().get(mission.getUID()));
        assertEquals(1_000, mission.getStart());
    }

    @Test
    void differentMissionsCannotSharePendingUid() {
        UUID uid = UUID.randomUUID();
        TestMission first = new TestMission(uid);
        TestMission second = new TestMission(uid);
        first.onTimeout(() -> {
        });
        second.onTimeout(() -> {
        });

        first.run(new Object());

        assertThrows(IllegalStateException.class, () -> second.run(new Object()));
        assertEquals(1, Porticus.INSTANCE.getMissions().size());
        assertSame(first, Porticus.INSTANCE.getMissions().get(uid));
    }

    @Test
    void missionCanOnlyBeFinalizedOnce() {
        TestMission mission = new TestMission();
        mission.now = 1_000;
        mission.onTimeout(() -> {
        });
        mission.run(new Object());

        assertTrue(mission.cancel());
        assertFalse(mission.cancel());
    }

    @Test
    void missionCannotBeReusedAfterFinalization() {
        TestMission mission = new TestMission();
        mission.onTimeout(() -> {
        });
        mission.now = 1_000;
        mission.run(new Object());
        assertTrue(mission.cancel());

        mission.now = 2_000;

        assertThrows(IllegalStateException.class, () -> mission.run(new Object()));
        assertFalse(mission.pending());
        assertEquals(1_000, mission.getStart());
    }

    private static class TestMission extends PorticusMission {

        private long now;

        private TestMission() {
            timeSource = () -> now;
        }

        private TestMission(UUID uid) {
            super(uid);
            timeSource = () -> now;
        }

        private boolean cancel() {
            return Porticus.INSTANCE.getMissions().remove(getUID(), this);
        }

        private boolean pending() {
            return isPending();
        }
    }
}
