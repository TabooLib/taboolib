package me.skymc.taboolib.common.versioncontrol;

import org.objectweb.asm.*;

import java.util.stream.IntStream;

/**
 * 我不信 ClassNotFound 的邪，自己写了一个发现还是一样。。。
 *
 * @Author sky
 * @Since 2018-9-19 21:33
 */
public class SimpleMethodVisitor extends MethodVisitor {

    private final SimpleVersionControl simpleVersionControl;

    public SimpleMethodVisitor(SimpleVersionControl simpleVersionControl, MethodVisitor methodVisitor) {
        super(Opcodes.ASM5, methodVisitor);
        this.simpleVersionControl = simpleVersionControl;
    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(translate(name), access);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        super.visitMethodInsn(opcode, translate(owner), translate(name), translate(descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, translate(owner), translate(name), translate(descriptor), isInterface);
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof Type) {
            super.visitLdcInsn(Type.getType(translate(((Type) value).getDescriptor())));
        } else {
            super.visitLdcInsn(value);
        }
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, translate(type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, translate(owner), translate(name), translate(descriptor));
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(translate(name), translate(descriptor), translate(signature), start, end, index);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(translate(name), translate(descriptor), bootstrapMethodHandle, bootstrapMethodArguments);
    }

    private String translate(String target) {
        return target == null ? null : simpleVersionControl.replace(target);
    }

    private String[] translate(String[] target) {
        if (target == null) {
            return target;
        }
        IntStream.range(0, target.length).forEach(i -> target[i] = translate(target[i]));
        return target;
    }
}