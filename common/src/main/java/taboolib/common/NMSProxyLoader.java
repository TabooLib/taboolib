package taboolib.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/** 非隔离模式在业务类加载前转译资源，并交给插件原加载器定义。 */
public class NMSProxyLoader {

    private static final String PREFIX = "META-INF/taboolib/nms-proxy/";
    private static boolean initialized;

    public static synchronized void init() throws Throwable {
        if (initialized || PrimitiveSettings.IS_ISOLATED_MODE) {
            return;
        }
        File source = new File(TabooLib.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!source.isFile()) {
            return;
        }
        // 只读取当前插件的索引，避免读到其他插件的同名资源。
        try (JarFile jar = new JarFile(source)) {
            JarEntry index = jar.getJarEntry(PREFIX + "index");
            if (index == null) {
                return;
            }
            ClassLoader loader = ClassAppender.getClassLoader();
            Class<?> runtime = Class.forName("taboolib.module.nms.NMSProxyRuntime", true, loader);
            Method transform = runtime.getMethod("transform", String.class, byte[].class);
            File translated = File.createTempFile("taboolib-nms-proxy-", ".jar");
            boolean attached = false;
            try {
                Manifest manifest = jar.getManifest();
                try (JarOutputStream output = manifest == null
                        ? new JarOutputStream(Files.newOutputStream(translated.toPath()))
                        : new JarOutputStream(Files.newOutputStream(translated.toPath()), manifest);
                     BufferedReader names = new BufferedReader(new InputStreamReader(jar.getInputStream(index), StandardCharsets.UTF_8))) {
                    String name;
                    while ((name = names.readLine()) != null) {
                        if (name.trim().isEmpty()) {
                            continue;
                        }
                        String path = name.replace('.', '/');
                        JarEntry entry = jar.getJarEntry(PREFIX + path + ".bin");
                        if (entry == null) {
                            throw new IllegalStateException("Missing packaged NMSProxy class: " + name);
                        }
                        byte[] bytes;
                        try (InputStream input = jar.getInputStream(entry); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                            byte[] chunk = new byte[8192];
                            int count;
                            while ((count = input.read(chunk)) != -1) {
                                buffer.write(chunk, 0, count);
                            }
                            bytes = buffer.toByteArray();
                        }
                        try {
                            bytes = (byte[]) transform.invoke(null, name, bytes);
                        } catch (InvocationTargetException ex) {
                            throw new IllegalStateException("NMSProxy translation failed: " + name, ex.getCause());
                        }
                        output.putNextEntry(new JarEntry(path + ".class"));
                        output.write(bytes);
                        output.closeEntry();
                    }
                }
                // 全部转译成功后再注入，父类、内部类和普通 new 均由原加载器解析。
                ClassAppender.addPath(translated.toPath(), false, true);
                attached = true;
                translated.deleteOnExit();
                initialized = true;
            } finally {
                if (!attached) {
                    Files.deleteIfExists(translated.toPath());
                }
            }
        }
    }
}
