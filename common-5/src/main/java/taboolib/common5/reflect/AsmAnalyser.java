package taboolib.common5.reflect;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class AsmAnalyser extends ClassVisitor implements Opcodes {

    private final List<String> fields = new ArrayList<>();
    private final List<String> methods = new ArrayList<>();

    private final int excludeModifier;

    public AsmAnalyser(ClassVisitor classVisitor, int excludeModifiers) {
        super(Opcodes.ASM6, classVisitor);
        this.excludeModifier = excludeModifiers;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & excludeModifier) == 0) {
            fields.add(name);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if ((access & excludeModifier) == 0) {
            methods.add(name);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    public List<String> getFields() {
        return fields;
    }

    public List<String> getMethods() {
        return methods;
    }
}
