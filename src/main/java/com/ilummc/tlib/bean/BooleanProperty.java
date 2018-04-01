package com.ilummc.tlib.bean;

import java.util.function.BiConsumer;

public class BooleanProperty {

    private boolean property;

    public BooleanProperty(boolean property) {
        this.property = property;
    }

    public void addListener(BiConsumer<Boolean, Boolean> consumer) {

    }

}
