package me.skymc.taboolib.common.util;

/**
 * @Author sky
 * @Since 2018-09-25 15:21
 */
public class SimpleCounter {

    private int timer;
    private int limit;

    public SimpleCounter(int limit) {
        this.timer = 0;
        this.limit = limit;
    }

    public boolean next() {
        if (--timer <= 0) {
            timer = limit;
            return true;
        }
        return false;
    }

    public void reset() {
        timer = 0;
    }
}
