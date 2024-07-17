package taboolib.library.kether;

import java.util.Objects;

public class ExitStatus {

    private static final ExitStatus PAUSED = new ExitStatus(true, false, 0);

    private final boolean running;
    private final boolean waiting;
    private final long startTime;

    public ExitStatus(boolean running, boolean waiting, long startTime) {
        this.running = running;
        this.waiting = waiting;
        this.startTime = startTime;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExitStatus that = (ExitStatus) o;
        return isRunning() == that.isRunning() &&
            isWaiting() == that.isWaiting() &&
            getStartTime() == that.getStartTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRunning(), isWaiting(), getStartTime());
    }

    @Override
    public String toString() {
        return "ExitStatus{" +
            "running=" + running +
            ", waiting=" + waiting +
            ", startTime=" + startTime +
            '}';
    }

    public static ExitStatus success() {
        return new ExitStatus(false, false, 0);
    }

    public static ExitStatus paused() {
        return PAUSED;
    }

    public static ExitStatus cooldown(long timeout) {
        return new ExitStatus(true, true, System.currentTimeMillis() + timeout);
    }
}
