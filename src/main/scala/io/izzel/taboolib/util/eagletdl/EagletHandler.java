package io.izzel.taboolib.util.eagletdl;

@FunctionalInterface
public interface EagletHandler<T> {

    void handle(T event) ;

}
