package io.izzel.taboolib.test;

import io.izzel.taboolib.kotlin.Reflex;

public class ReflexTest {

    public static final String a = "??";

    public static void main(String[] args) {
        Reflex reflex = new Reflex(ReflexTest.class);
        reflex.set("a", "sb");

        System.out.println(a);
        System.out.println(reflex.<String>get(a));

        reflex.invoke("run");
    }

    void run() {
        System.out.println(" run " + a);
    }
}

