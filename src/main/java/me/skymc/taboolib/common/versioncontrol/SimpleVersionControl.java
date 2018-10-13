package me.skymc.taboolib.common.versioncontrol;

import com.google.common.collect.Lists;
import com.ilummc.tlib.util.asm.AsmClassLoader;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author sky
 * @Since 2018-09-19 21:05
 */
public class SimpleVersionControl {

    private String target;
    private List<String> from = Lists.newArrayList();
    private String to;
    private Plugin plugin;

    SimpleVersionControl() {
    }

    public static SimpleVersionControl create() {
        return new SimpleVersionControl().to(TabooLib.getVersion()).plugin(Main.getInst());
    }

    public static SimpleVersionControl create(String toVersion) {
        return new SimpleVersionControl().to(toVersion).plugin(Main.getInst());
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
        this.from.add(from.startsWith("v") ? from : "v" + from);
        return this;
    }

    public SimpleVersionControl to(String to) {
        this.to = to.startsWith("v") ? to : "v" + to;
        return this;
    }

    public SimpleVersionControl plugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public Class<?> translate() throws IOException {
        return translate(plugin);
    }

    public Class<?> translate(Plugin plugin) throws IOException {
        ClassReader classReader = new ClassReader(FileUtils.getResource(plugin, target.replace(".", "/") + ".class"));
        ClassWriter classWriter = new ClassWriter(0);
        ClassVisitor classVisitor = new SimpleClassVisitor(this, classWriter);
        classReader.accept(classVisitor, 0);
        classWriter.visitEnd();
        classVisitor.visitEnd();
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

    public List<String> getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String replace(String origin) {
        for (String from : from) {
            origin = origin.replace("/" + from + "/", "/" + to + "/");
        }
        return origin;
    }
}
