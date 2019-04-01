package me.skymc.taboolib.common.versioncontrol;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.stream.IntStream;

/**
 * 我不信 ClassNotFoundException 的邪，自己写了一个发现还是一样。。。
 *
 * @Author sky
 * @Since 2018-09-19 21:17
 */
public class SimpleClassVisitor extends ClassVisitor {

    private final SimpleVersionControl simpleVersionControl;

    public SimpleClassVisitor(SimpleVersionControl simpleVersionControl, ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
        this.simpleVersionControl = simpleVersionControl;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, translate(name), translate(signature), translate(superName), translate(interfaces));
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(translate(name), translate(outerName), translate(innerName), access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return super.visitField(access, translate(name), translate(descriptor), translate(signature), value instanceof String ? translate((String) value) : value);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        super.visitOuterClass(translate(owner), translate(name), translate(descriptor));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new SimpleMethodVisitor(simpleVersionControl, super.visitMethod(access, translate(name), translate(descriptor), translate(signature), translate(exceptions)));
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
