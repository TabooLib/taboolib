package io.izzel.taboolib.util.asm;


import org.bukkit.Bukkit;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class AsmClassTransformer extends ClassVisitor implements Opcodes {

    private final Class<?> from;
    private final String fromVer;
    private final String toVer;
    private final ClassWriter writer;
    private String newClassName;
    private String prevName;

    private AsmClassTransformer(Class<?> from, String fromVer, String toVer, ClassWriter classWriter) {
        super(Opcodes.ASM5, classWriter);
        this.writer = classWriter;
        this.from = from;
        this.fromVer = fromVer;
        this.toVer = toVer;
    }

    public Object transform() {
        try {
            ClassReader classReader = new ClassReader(from.getResourceAsStream("/" + from.getName().replace('.', '/') + ".class"));
            newClassName = from.getName() + "_TabooLibRemap_" + this.hashCode() + "_" + toVer;
            prevName = from.getName().replace('.', '/');
            classReader.accept(this, ClassReader.SKIP_DEBUG);
            Class<?> clazz = AsmClassLoader.createNewClass(newClassName, writer.toByteArray());
            Field field = from.getClassLoader().getClass().getDeclaredField("classes");
            field.setAccessible(true);
            ((Map<String, Class<?>>) field.get(from.getClassLoader())).put(newClassName, clazz);
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (IOException | NoSuchFieldException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Builder builder() {
        return new Builder().toVersion(Bukkit.getServer().getClass().getName().split("\\.")[3]);
    }

    public static Builder builder(String ver) {
        return new Builder().toVersion(ver);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, replace(descriptor), replace(signature), replace(exceptions));
        return new AsmMethodTransformer(visitor);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return super.visitField(access, name, replace(descriptor), replace(signature), value);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(replace(name), outerName, replace(name).substring(outerName.length() + 1), access);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, newClassName.replace('.', '/'), replace(signature), replace(superName), replace(interfaces));
    }

    private String replace(String text) {
        if (text != null) {
            return text
                    .replace("net/minecraft/server/" + fromVer, "net/minecraft/server/" + toVer)
                    .replace("org/bukkit/craftbukkit/" + fromVer, "org/bukkit/craftbukkit/" + toVer)
                    .replace(prevName, newClassName.replace('.', '/'));
        } else {
            return null;
        }
    }

    private String[] replace(String[] text) {
        if (text != null) {
            for (int i = 0; i < text.length; i++) {
                text[i] = replace(text[i]);
            }
            return text;
        } else {
            return null;
        }
    }

    private class AsmMethodTransformer extends MethodVisitor {

        AsmMethodTransformer(MethodVisitor visitor) {
            super(Opcodes.ASM5, visitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, replace(owner), name, replace(descriptor), isInterface);
        }

        @Override
        public void visitLdcInsn(Object value) {
            if (value instanceof String) {
                super.visitLdcInsn(replace((String) value));
            } else {
                super.visitLdcInsn(value);
            }
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, replace(type));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            super.visitFieldInsn(opcode, replace(owner), name, replace(descriptor));
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            super.visitLocalVariable(name, replace(descriptor), replace(signature), start, end, index);
        }
    }

    public static class Builder {

        private Class<?> from;

        private String fromVersion, toVersion;

        public Builder from(Class<?> clazz) {
            this.from = clazz;
            return this;
        }

        public Builder fromVersion(String ver) {
            fromVersion = ver;
            return this;
        }

        public Builder toVersion(String ver) {
            toVersion = ver;
            return this;
        }

        public AsmClassTransformer build() {
            return new AsmClassTransformer(from, fromVersion, toVersion, new ClassWriter(ClassWriter.COMPUTE_MAXS));
        }

    }
}
