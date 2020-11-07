package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Strings;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.net.ConnectException;
import java.util.Objects;

/**
 * 依赖注入工具
 *
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
                    } catch (ConnectException t) {
                        System.out.println("[TabooLib] " + Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("DEPENDENCY-DOWNLOAD-FAIL"), plugin.getName(), dependency.maven()));
                    }
                }
            }
        } else {
            try {
                ClassReader classReader = new ClassReader(Objects.requireNonNull(Files.getResource(plugin, clazz.getName().replace(".", "/") + ".class")));
                ClassVisitor classVisitor = new DependencyClassVisitor(plugin, null);
                classReader.accept(classVisitor, ClassReader.SKIP_CODE);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
