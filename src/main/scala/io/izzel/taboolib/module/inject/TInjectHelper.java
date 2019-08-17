package io.izzel.taboolib.module.inject;

/**
 * @Author sky
 * @Since 2019-08-17 23:22
 */
public class TInjectHelper {

    enum State {

        PRE, POST, ACTIVE, CANCEL
    }

    public static String fromState(TInject inject, State state) {
        switch (state) {
            case PRE:
                return inject.load();
            case POST:
                return inject.init();
            case ACTIVE:
                return inject.active();
            default:
                return inject.cancel();
        }
    }
}
