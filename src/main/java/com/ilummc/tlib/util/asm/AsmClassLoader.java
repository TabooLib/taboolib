package com.ilummc.tlib.util.asm;

public class AsmClassLoader extends ClassLoader {

    public AsmClassLoader() {
        super(AsmClassLoader.class.getClassLoader());
    }

    public Class<?> createNewClass(String name, byte[] arr) {
        return defineClass(name, arr, 0, arr.length, AsmClassLoader.class.getProtectionDomain());
    }

}
