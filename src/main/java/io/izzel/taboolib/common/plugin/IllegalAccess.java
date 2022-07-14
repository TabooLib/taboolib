package io.izzel.taboolib.common.plugin;

import com.google.common.collect.Lists;
import io.izzel.taboolib.Version;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.List;

/**
 * @author sky
 * @since 2020-07-28 15:37
 */
public class IllegalAccess {

    private static final List<String> blocked = Lists.newArrayList("softdepend or loadbefore of this plugin");

    public static List<String> getBlocked() {
        return blocked;
    }

    public static void init() {
        if (Version.isBefore(Version.v1_13)) {
            return;
        }
        ((Logger) LogManager.getRootLogger()).addFilter(new Filter() {

            public Result check(String message) {
                for (String line : blocked) {
                    if (message.contains(line)) {
                        return Result.DENY;
                    }
                }
                return Result.NEUTRAL;
            }

            @Override
            public State getState() {
                try {
                    return LifeCycle.State.STARTED;
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            public void initialize() {

            }

            @Override
            public void start() {

            }

            @Override
            public void stop() {

            }

            @Override
            public boolean isStarted() {
                return true;
            }

            @Override
            public boolean isStopped() {
                return false;
            }

            @Override
            public Result getOnMismatch() {
                return Result.NEUTRAL;
            }

            @Override
            public Result getOnMatch() {
                return Result.NEUTRAL;
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9) {
                return check(s);
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable) {
                return check(o.toString());
            }

            @Override
            public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
                return check(message.getFormattedMessage());
            }

            @Override
            public Result filter(LogEvent logEvent) {
                return check(logEvent.getMessage().getFormattedMessage());
            }
        });
    }
}
