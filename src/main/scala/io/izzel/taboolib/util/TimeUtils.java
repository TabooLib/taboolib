package io.izzel.taboolib.util;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

/**
 * @author izzel
 */
public class TimeUtils {

    public static final TemporalUnit TICK = new TemporalUnit() {

        @Override
        public Duration getDuration() {
            return Duration.ofMillis(50);
        }

        @Override
        public boolean isDurationEstimated() {
            return false;
        }

        @Override
        public boolean isDateBased() {
            return false;
        }

        @Override
        public boolean isTimeBased() {
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R addTo(R temporal, long amount) {
            return (R) temporal.plus(amount, this);
        }

        @Override
        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            return temporal1Inclusive.until(temporal2Exclusive, this);
        }

        @Override
        public String toString() {
            return "Ticks";
        }
    };
}
