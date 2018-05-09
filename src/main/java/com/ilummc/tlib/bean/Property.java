package com.ilummc.tlib.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Property<T> {

    private Property(T value) {
        this.value = value;
    }

    private List<BiConsumer<T, T>> consumers;

    private T value;

    public void set(T value) {
        if (value != this.value) {
            if (consumers != null) {
                for (BiConsumer<T, T> consumer : consumers) {
                    consumer.accept(this.value, value);
                }
            }
            this.value = value;
        }
    }

    public T get() {
        return value;
    }

    public void addListener(BiConsumer<T, T> consumer) {
        if (consumers == null) {
            consumers = new ArrayList<>();
        }
        consumers.add(consumer);
    }

    public static <T> Property<T> of(T value) {
        return new Property<>(value);
    }

}
