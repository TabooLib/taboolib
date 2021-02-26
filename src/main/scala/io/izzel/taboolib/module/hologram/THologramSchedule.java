package io.izzel.taboolib.module.hologram;

import io.izzel.taboolib.module.packet.Packet;

import java.util.concurrent.Callable;

/**
 * @author sky
 * @since 2020-03-07 14:28
 */
abstract class THologramSchedule {

    private final Callable<Boolean> condition;

    public THologramSchedule(Callable<Boolean> condition) {
        this.condition = condition;
    }

    public boolean check() {
        try {
            return condition.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    abstract public void before();

    abstract public void after(Packet packet);
}
