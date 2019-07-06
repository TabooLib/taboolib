package io.izzel.taboolib.util.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class AsmAnalyser extends ClassVisitor implements Opcodes {

    private final List<String> fields = new ArrayList<>();

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

    public List<String> getFields() {
        return fields;
    }
}
