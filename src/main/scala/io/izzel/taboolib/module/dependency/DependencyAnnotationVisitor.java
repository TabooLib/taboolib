package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.util.Strings;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @Author 坏黑
 * @Since 2019-07-13 15:25
 */
public class DependencyAnnotationVisitor extends AnnotationVisitor {

    private Plugin plugin;
    private String maven;
    private String mavenRepo = TDependency.MAVEN_REPO;
    private String url = "";

    public DependencyAnnotationVisitor(Plugin plugin, AnnotationVisitor annotationVisitor) {
        super(Opcodes.ASM5, annotationVisitor);
        this.plugin = plugin;
    }

    @Override
    public void visit(String name, Object value) {
        switch (name) {
            case "maven":
                maven = String.valueOf(value);
                break;
            case "mavenRepo":
                mavenRepo = String.valueOf(value);
                break;
            case "url":
                url = String.valueOf(value);
                break;
        }
        super.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return new DependencyAnnotationVisitor(plugin, super.visitAnnotation(name, descriptor));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new DependencyAnnotationVisitor(plugin, super.visitArray(name));
    }

    @Override
    public void visitEnd() {
        if (maven != null) {
            if (TDependency.requestLib(maven, mavenRepo, url)) {
                TabooLibAPI.debug("  Loaded " + String.join(":", maven) + " (" + plugin.getName() + ")");
            } else {
                TabooLib.getLogger().warn(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("DEPENDENCY-LOAD-FAIL"), plugin.getName(), String.join(":", maven)));
            }
            maven = null;
            mavenRepo = TDependency.MAVEN_REPO;
            url = "";
        }
        super.visitEnd();
    }
}
