package io.izzel.taboolib.module.dependency;

import org.bukkit.plugin.Plugin;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author sky
 * @since 2019-7-13 15:17
 */
public class DependencyClassVisitor extends ClassVisitor {

    private final Plugin plugin;

    public DependencyClassVisitor(Plugin plugin, ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
        this.plugin = plugin;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new DependencyAnnotationVisitor(plugin, super.visitAnnotation(descriptor, visible));
    }
}
