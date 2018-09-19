package me.skymc.taboolib.common.versioncontrol;

import com.ilummc.tlib.util.asm.AsmClassLoader;
import me.skymc.taboolib.TabooLib;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

/**
 * 我不信 ClassNotFound 的邪，自己写了一个发现还是一样。。。
 *
 * @Author sky
 * @Since 2018-09-19 21:05
 */
public class SimpleVersionControl {

    private String target;
    private String from;
    private String to;

    SimpleVersionControl() {
    }

    public static SimpleVersionControl create() {
        return new SimpleVersionControl().to(TabooLib.getVersion());
    }

    public static SimpleVersionControl create(String toVersion) {
        return new SimpleVersionControl().to(toVersion);
    }

    public SimpleVersionControl target(Class<?> target) {
        this.target = target.getName();
        return this;
    }

    public SimpleVersionControl target(String target) {
        this.target = target;
        return this;
    }

    public SimpleVersionControl from(String from) {
        this.from = from.startsWith("v") ? from : "v" + from;
        return this;
    }

    public SimpleVersionControl to(String to) {
        this.to = to.startsWith("v") ? to : "v" + to;
        return this;
    }

    public Class<?> translate() throws IOException {
        ClassReader classReader = new ClassReader(target);
        ClassWriter classWriter = new ClassWriter(0);
        ClassVisitor classVisitor = new SimpleClassVisitor(this, classWriter);
        classReader.accept(classVisitor, 0);
        return AsmClassLoader.createNewClass(target, classWriter.toByteArray());
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getTarget() {
        return target;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
