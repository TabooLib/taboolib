package io.izzel.taboolib.util.asm;

import com.google.common.collect.Lists;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.common.plugin.bridge.BridgeLoader;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.IO;
import io.izzel.taboolib.util.Pair;
import io.izzel.taboolib.util.Ref;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ASM 版本转换工具
 *
 * @author sky
 * @since 2018-09-19 21:05
 */
public class AsmVersionControl {

    private static final Map<String, byte[]> cacheClasses = new HashMap<>();
    private String target;
    private String to;
    private Plugin plugin;
    private boolean useCache;
    private boolean useNMS;
    private boolean mapping;
    private final List<String> from = Lists.newArrayList();
    private final List<Pair<String, String>> mappingList = Lists.newArrayList();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    AsmVersionControl() {
        useCache = false;
    }

    public static AsmVersionControl create() {
        return new AsmVersionControl().to(Version.getBukkitVersion()).plugin(TabooLib.getPlugin());
    }

    public static AsmVersionControl create(String toVersion) {
        return new AsmVersionControl().to(toVersion).plugin(TabooLib.getPlugin());
    }

    public static AsmVersionControl createSimple(String target, String... from) {
        return create().target(target).from(from);
    }

    public static AsmVersionControl createNMS(String target) {
        return create().target(target).useNMS();
    }

    /**
     * 设置转换类地址，写法如：me.skymc.taboolib.packet.InternalPacket
     *
     * @param target 转换类地址
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl target(String target) {
        this.target = target;
        return this;
    }

    /**
     * 设置原版本，写法如：v1_8_R3
     *
     * @param from 原始版本
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl from(String from) {
        this.from.add(from.startsWith("v") ? from : "v" + from);
        return this;
    }

    /**
     * 设置原版本，写法如：v1_8_R3, v1_12_R1
     *
     * @param from 原始版本
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl from(String... from) {
        Arrays.stream(from).forEach(v -> this.from.add(v.startsWith("v") ? v : "v" + v));
        return this;
    }

    /**
     * 设置目标版本
     *
     * @param to 目标版本
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl to(String to) {
        this.to = to.startsWith("v") ? to : "v" + to;
        return this;
    }

    /**
     * 设置插件，不填默认指向 TabooLib
     *
     * @param plugin 插件
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl plugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    /**
     * 转换类将会保存在 TabooLib 中，防止出现 NoClassDefFoundError 异常
     *
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl useCache() {
        this.useCache = true;
        return this;
    }

    /**
     * 自动转换所有使用到的 NMS 或 OBC 方法
     *
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl useNMS() {
        this.useNMS = true;
        return this;
    }

    /**
     * 将转换结果打印至 plugins/TabooLib/asm-mapping
     *
     * @return {@link AsmVersionControl}
     */
    public AsmVersionControl mapping() {
        this.mapping = true;
        return this;
    }

    public Class<?> translate() throws IOException {
        return translate(plugin);
    }

    public void a(ArmorStand a) {
    }

    public Class<?> translate(Plugin plugin) throws IOException {
        // 防止出现 Class not found 的奇葩问题，使用缓存（目的是兼容热重载）
        InputStream inputStream = useCache ? new ByteArrayInputStream(cacheClasses.computeIfAbsent(target, n -> {
            try {
                return IO.readFully(Objects.requireNonNull(Files.getResource(plugin, target.replace(".", "/") + ".class")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new byte[0];
        })) : Files.getResource(plugin, target.replace(".", "/") + ".class");
        // 读取
        ClassReader classReader = new ClassReader(Objects.requireNonNull(inputStream));
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ClassRemapper(classWriter, new VersionRemapper(this));
        classReader.accept(classVisitor, 0);
        // 打印
        if (mapping || plugin instanceof InternalPlugin) {
            executorService.submit(this::printMapping);
        }
        // 因第三方插件调用该方法时会出现找不到类，所以第三方插件使用 BridgeLoader 加载类
        return plugin instanceof InternalPlugin ? AsmClassLoader.createNewClass(target, classWriter.toByteArray()) : BridgeLoader.createNewClass(target, classWriter.toByteArray());
    }

    public Class<?> translateBridge() throws IOException {
        Class<?> callerClass = Ref.getCallerClass(3).orElse(null);
        if (callerClass != null && !callerClass.getName().startsWith("io.izzel")) {
            throw new IllegalStateException();
        }
        ClassReader classReader = new ClassReader(Objects.requireNonNull(Files.getTabooLibResource(target.replace(".", "/") + ".class")));
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ClassRemapper(classWriter, new VersionRemapper(this));
        classReader.accept(classVisitor, 0);
        if (mapping || plugin instanceof InternalPlugin) {
            executorService.submit(this::printMapping);
        }
        return BridgeLoader.createNewClass(target, classWriter.toByteArray());
    }

    public String replace(String origin) {
        String replace = origin;
        if (useNMS) {
            replace = replace
                    .replaceAll("net/minecraft/server/.*?/", "net/minecraft/server/" + to + "/")
                    .replaceAll("org/bukkit/craftbukkit/.*?/", "org/bukkit/craftbukkit/" + to + "/");
        }
        for (String from : from) {
            replace = replace.replace("/" + from + "/", "/" + to + "/");
        }
        if ((mapping || plugin instanceof InternalPlugin) && !replace.equals(origin)) {
            mappingList.add(new Pair<>(origin, replace));
        }
        return replace;
    }

    public void printMapping() {
        if (mappingList.isEmpty()) {
            return;
        }
        List<Pair<String, String>> list = Lists.newArrayList(mappingList);
        list.sort((b, a) -> Integer.compare(a.getKey().length(), b.getKey().length()));
        int length = list.get(0).getKey().length();
        Files.write(Files.file(TabooLib.getPlugin().getDataFolder(), "asm-mapping/" + target.replace("/", ".") + ".txt"), b -> {
            for (Pair<String, String> pair : mappingList) {
                StringBuilder builder = new StringBuilder(pair.getKey());
                for (int i = pair.getKey().length(); i < length; i++) {
                    builder.append(" ");
                }
                b.write(builder.toString() + " -> " + pair.getValue());
                b.newLine();
            }
        });
    }

    public String getTarget() {
        return target;
    }

    public List<String> getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public boolean isUseNMS() {
        return useNMS;
    }

    public boolean isMapping() {
        return mapping;
    }

    public List<Pair<String, String>> getMappingList() {
        return mappingList;
    }
}