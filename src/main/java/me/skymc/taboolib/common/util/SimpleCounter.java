package me.skymc.taboolib.common.util;

/**
 * @Author sky
 * @Since 2018-09-25 15:21
 */
public class SimpleCounter {

    private int timer;
    private int limit;
    private boolean ignoredFirst;
    private boolean counterFirst;

    public SimpleCounter(int limit) {
        this(limit, false);
    }

    public SimpleCounter(int limit, boolean ignoredFirst) {
        this.timer = 0;
        this.limit = limit;
        this.ignoredFirst = ignoredFirst;
        this.counterFirst = true;
    }

    public boolean next() {
        if (--timer <= 0) {
            timer = limit;
            if (ignoredFirst && counterFirst) {
                counterFirst = false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        timer = 0;
    }
}
