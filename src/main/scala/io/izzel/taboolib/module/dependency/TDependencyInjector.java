package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.util.Files;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * @author Izzel_Aliz
 */
public class TDependencyInjector {

    public static void inject(Plugin plugin, Class<?> clazz) {
        if (clazz.equals(TabooLib.class)) {
            for (Dependency dependency : TabooLib.class.getAnnotationsByType(Dependency.class)) {
                for (String url : dependency.url().split(";")) {
                    try {
                        if (TDependency.requestLib(dependency.maven(), dependency.mavenRepo(), url)) {
                            break;
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        } else {
            try {
                ClassReader classReader = new ClassReader(Files.getResource(plugin, clazz.getName().replace(".", "/") + ".class"));
                ClassWriter classWriter = new ClassWriter(0);
                ClassVisitor classVisitor = new DependencyClassVisitor(plugin, classWriter);
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
                classWriter.visitEnd();
                classVisitor.visitEnd();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
