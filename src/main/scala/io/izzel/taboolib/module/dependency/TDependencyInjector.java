package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.util.Files;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * @author Izzel_Aliz
 */
public class TDependencyInjector {

    private static boolean libInjected;

    public static void inject(Plugin plugin, Class<?> clazz) {
        if (!plugin.getName().equals("TabooLib") || !libInjected) {
            try {
                ClassReader classReader = new ClassReader(Files.getResource(plugin, clazz.getName().replace(".", "/") + ".class"));
                ClassWriter classWriter = new ClassWriter(0);
                ClassVisitor classVisitor = new DependencyClassVisitor(plugin, classWriter);
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
                classWriter.visitEnd();
                classVisitor.visitEnd();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                libInjected = true;
            }
        }
    }
}
