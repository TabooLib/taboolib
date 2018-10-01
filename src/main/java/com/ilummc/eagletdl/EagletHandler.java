package com.ilummc.eagletdl;

@FunctionalInterface
public interface EagletHandler<T> {

    void handle(T event) ;

}
