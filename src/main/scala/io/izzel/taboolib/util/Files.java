package io.izzel.taboolib.util;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.module.db.local.SecuredFile;
import io.izzel.taboolib.module.inject.TFunction;
import me.skymc.taboolib.plugin.PluginUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具
 *
 * @author sky
 */
@SuppressWarnings("rawtypes")
public class Files {

    /**
     * 读取一个插件的所有类，该插件可以不基于 TabooLib
     *
     * @param plugin 插件实例
     */
    @NotNull
    public static List<Class> getClasses(Plugin plugin) {
        return getClasses(plugin, new String[0]);
    }

    /**
     * 读取一个插件的所有类，该插件可以不基于 TabooLib，并选择忽略的包名。
     *
     * @param plugin 插件实例
     * @param ignore 忽略包名，左模糊判断
     */
    @NotNull
    public static List<Class> getClasses(Plugin plugin, String[] ignore) {
        List<Class> classes = new CopyOnWriteArrayList<>();
        URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            File src;
            try {
                src = new File(url.toURI());
            } catch (URISyntaxException e) {
                src = new File(url.getPath());
            }
            new JarFile(src).stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                try {
                    if (Arrays.stream(ignore).noneMatch(className::startsWith)) {
                        classes.add(Class.forName(className, false, plugin.getClass().getClassLoader()));
                    }
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
        return classes;
    }

    /**
     * 清理缓存文件，该方法会在服务端关闭时自动执行
     */
    @TFunction.Cancel
    public static void clearTempFiles() {
        deepDelete(new File("plugins/TabooLib/temp"));
    }

    /**
     * 获取 TabooLib 资源文件
     *
     * @param filename 资源文件名称
     */
    @Nullable
    public static InputStream getResource(String filename) {
        return getResource(TabooLib.getPlugin(), filename);
    }

    /**
     * 获取插件资源文件，这个插件可以是 TabooLib 的伪装插件 {@link InternalPlugin}
     *
     * @param plugin   插件 实例
     * @param filename 资源文件名称
     */
    @Nullable
    public static InputStream getResource(Plugin plugin, String filename) {
        return plugin instanceof InternalPlugin ? getTabooLibResource(filename) : plugin.getClass().getClassLoader().getResourceAsStream(filename);
    }

    /**
     * 获取插件资源文件，这个插件可以是 TabooLib 的伪装插件 {@link InternalPlugin}
     * 如果是 TabooLib 的伪装插件则会自动添加 "__resources__/" 前缀
     *
     * @param plugin   插件 实例
     * @param filename 资源文件名称
     */
    @Nullable
    public static InputStream getResourceChecked(Plugin plugin, String filename) {
        return plugin instanceof InternalPlugin ? getResource(plugin, "__resources__/" + filename) : getResource(plugin, filename);
    }

    /**
     * 从一份拷贝中获取 TabooLib 真实资源文件
     *
     * @param filename 资源文件名称
     */
    @Nullable
    public static InputStream getTabooLibResource(String filename) {
        return getCanonicalResource(TabooLib.getPlugin(), filename);
    }

    /**
     * 从一份拷贝中获取插件的真实资源文件，这个插件可以是 TabooLib 的伪装插件
     *
     * @param plugin   插件实例
     * @param filename 资源文件名称
     */
    @Nullable
    public static InputStream getCanonicalResource(Plugin plugin, String filename) {
        File file = file(new File("plugins/TabooLib/temp/" + UUID.randomUUID()));
        try {
            copy(plugin instanceof InternalPlugin ? new File("libs/TabooLib.jar") : PluginUtils.getPluginFile(plugin), file);
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(filename);
            if (entry != null) {
                return zipFile.getInputStream(entry);
            }
        } catch (Exception t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * 释放资源文件，这个插件可以是 TabooLib 的伪装插件
     *
     * @param plugin 插件实例
     * @param path   资源文件路径
     */
    @NotNull
    public static File releaseResource(Plugin plugin, String path) {
        releaseResource(plugin, path, false);
        return new File(plugin.getDataFolder(), path);
    }

    /**
     * 释放资源文件，这个插件可以是 TabooLib 的伪装插件
     *
     * @param plugin  插件实例
     * @param path    资源文件路径
     * @param replace 是否替换
     */
    public static void releaseResource(Plugin plugin, String path, boolean replace) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists() || replace) {
            try (InputStream inputStream = getCanonicalResource(plugin, (plugin instanceof InternalPlugin ? "__resources__/" : "") + path)) {
                if (inputStream != null) {
                    toFile(inputStream, file(file));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * 获取一个目录下的所有文件，非递归
     *
     * @param file 文件
     */
    @NotNull
    public static File[] listFile(File file) {
        File[] files = file.listFiles();
        return files == null ? new File[0] : files;
    }

    /**
     * 将字节数字写入文件
     *
     * @param in   字节数组
     * @param file 文件实例
     */
    @NotNull
    public static File toFile(byte[] in, File file) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file); BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            bufferedOutputStream.write(in);
            bufferedOutputStream.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return file;
    }

    /**
     * 将文本写入文件
     *
     * @param in   文本
     * @param file 文件实例
     */
    @NotNull
    public static File toFile(String in, File file) {
        try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(in);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return file;
    }

    /**
     * 通过流写入文件
     *
     * @param inputStream 流
     * @param file        文件实例
     */
    @NotNull
    public static File toFile(InputStream inputStream, File file) {
        try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            bos.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return file;
    }

    /**
     * 通过互联网地址写入文件
     *
     * @param in   地址
     * @param file 文件实例
     */
    public static boolean downloadFile(String in, File file) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            toFile(bufferedInputStream, file);
            return true;
        } catch (Throwable ignored) { }
        return false;
    }

    @NotNull
    public static File file(File path, String filePath) {
        return file(new File(path, filePath));
    }

    @NotNull
    public static File file(String filePath) {
        return file(new File(filePath));
    }

    @NotNull
    public static File file(File file) {
        if (!file.exists()) {
            folder(file);
            try {
                file.createNewFile();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return file;
    }

    @NotNull
    public static File folder(File path, String filePath) {
        return folder(new File(path, filePath));
    }

    @NotNull
    public static File folder(String filePath) {
        return folder(new File(filePath));
    }

    @NotNull
    public static File folder(File file) {
        if (!file.exists()) {
            String filePath = file.getPath();
            int index = filePath.lastIndexOf(File.separator);
            String folderPath;
            File folder;
            if ((index >= 0) && (!(folder = new File(filePath.substring(0, index))).exists())) {
                folder.mkdirs();
            }
        }
        return file;
    }

    /**
     * 通过 FileChannel 拷贝文件
     *
     * @param file1 文件实例
     * @param file2 文件实例
     */
    public static void copy(File file1, File file2) {
        try (FileInputStream fileIn = new FileInputStream(file1); FileOutputStream fileOut = new FileOutputStream(file2); FileChannel channelIn = fileIn.getChannel(); FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
    }

    /**
     * 通过 FileChannel 拷贝文件（通过递归）
     *
     * @param originFileName 文件路径
     * @param targetFileName 文件路径
     */
    public static void deepCopy(String originFileName, String targetFileName) {
        File originFile = new File(originFileName);
        File targetFile = new File(targetFileName);
        if (!targetFile.exists()) {
            if (!originFile.isDirectory()) {
                file(targetFile);
            } else {
                targetFile.mkdirs();
            }
        }
        if (originFile.isDirectory()) {
            Arrays.stream(listFile(originFile)).parallel().forEach(file -> {
                if (file.isDirectory()) {
                    deepCopy(file.getAbsolutePath(), targetFileName + "/" + file.getName());
                } else {
                    copy(file, new File(targetFileName + "/" + file.getName()));
                }
            });
        } else {
            copy(originFile, targetFile);
        }
    }

    /**
     * 删除文件（通过递归）
     *
     * @param file 文件
     */
    public static void deepDelete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        Arrays.stream(listFile(file)).parallel().forEach(Files::deepDelete);
        file.delete();
    }

    /**
     * 通过互联网地址获取文本内容
     *
     * @param url 地址
     * @param def 默认值
     */
    @Nullable
    public static String readFromURL(String url, @Nullable String def) {
        return Optional.ofNullable(readFromURL(url)).orElse(def);
    }

    /**
     * 通过互联网地址获取文本内容
     *
     * @param url     地址
     * @param charset 编码
     * @param def     默认值
     */
    @Nullable
    public static String readFromURL(String url, @NotNull Charset charset, @Nullable String def) {
        return Optional.ofNullable(readFromURL(url, charset)).orElse(def);
    }

    /**
     * 通过互联网地址获取文本内容（默认编码 UTF-8）
     *
     * @param url 地址
     */
    @Nullable
    public static String readFromURL(String url) {
        return readFromURL(url, StandardCharsets.UTF_8);
    }

    /**
     * 通过互联网地址获取文本内容
     *
     * @param url     地址
     * @param charset 编码
     */
    @Nullable
    public static String readFromURL(String url, Charset charset) {
        try (InputStream inputStream = new URL(url).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return new String(IO.readFully(bufferedInputStream), charset);
        } catch (UnknownHostException ignored) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文件内容
     *
     * @param file 文件实例
     */
    @Nullable
    public static String readFromFile(File file) {
        return readFromFile(file, 1024, StandardCharsets.UTF_8);
    }

    @Nullable
    public static String readFromFile(File file, int size) {
        return readFromFile(file, size, StandardCharsets.UTF_8);
    }

    @Nullable
    public static String readFromFile(File file, int size, Charset encode) {
        try (FileInputStream fin = new FileInputStream(file); BufferedInputStream bin = new BufferedInputStream(fin)) {
            return readFromStream(fin, size, encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static List<String> readToList(File file) {
        try (FileInputStream fin = new FileInputStream(file); InputStreamReader isr = new InputStreamReader(fin, StandardCharsets.UTF_8); BufferedReader bin = new BufferedReader(isr)) {
            return bin.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @NotNull
    public static List<String> readToList(InputStream inputStream) {
        try (InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8); BufferedReader bin = new BufferedReader(isr)) {
            return bin.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Nullable
    public static String readFromStream(InputStream in) {
        return readFromStream(in, 1024, StandardCharsets.UTF_8);
    }

    @Nullable
    public static String readFromStream(InputStream in, int size) {
        return readFromStream(in, size, StandardCharsets.UTF_8);
    }

    @Nullable
    public static String readFromStream(InputStream in, int size, @NotNull Charset encode) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] b = new byte[size];
            int i;
            while ((i = in.read(b)) > 0) {
                bos.write(b, 0, i);
            }
            return new String(bos.toByteArray(), encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void read(File file, @NotNull ReadHandle readHandle) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            readHandle.read(bufferedReader);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void read(InputStream in, @NotNull ReadHandle readHandle) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            readHandle.read(bufferedReader);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void write(File file, @NotNull WriteHandle writeHandle) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            writeHandle.write(bufferedWriter);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void writeAppend(File file, @NotNull WriteHandle writeHandle) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            writeHandle.write(bufferedWriter);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void write(OutputStream out, @NotNull WriteHandle writeHandle) {
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            writeHandle.write(bufferedWriter);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @NotNull
    public static String encodeYAML(FileConfiguration file) {
        return Base64Coder.encodeLines(file.saveToString().getBytes()).replaceAll("\\s+", "");
    }

    @NotNull
    public static FileConfiguration decodeYAML(String args) {
        return SecuredFile.loadConfiguration(Base64Coder.decodeString(args));
    }

    @NotNull
    public static FileConfiguration load(File file) {
        return loadYaml(file);
    }

    @NotNull
    public static YamlConfiguration loadYaml(File file) {
        return SecuredFile.loadConfiguration(file);
    }

    @Nullable
    public static String getFileHash(File file, @NotNull String algorithm) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, length);
            }
            byte[] md5Bytes = digest.digest();
            return new BigInteger(1, md5Bytes).toString(16);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * 压缩文件
     *
     * @param source 文件实例
     * @param target 目标文件实例
     */
    public static void toZip(File source, File target) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(target); ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            toZip(zipOutputStream, source, "");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 压缩目录
     *
     * @param source 目录实例
     * @param target 目标文件实例
     */
    public static void toZipSkipDirectory(File source, File target) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(target); ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            if (source.isDirectory()) {
                Arrays.stream(listFile(source)).forEach(f -> toZip(zipOutputStream, f, ""));
            } else {
                toZip(zipOutputStream, source, "");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void toZip(ZipOutputStream zipOutputStream, File file, String path) {
        if (file.isDirectory()) {
            Arrays.stream(listFile(file)).forEach(f -> toZip(zipOutputStream, f, path + file.getName() + "/"));
        } else {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                zipOutputStream.putNextEntry(new ZipEntry(path + file.getName()));
                zipOutputStream.write(IO.readFully(fileInputStream));
                zipOutputStream.flush();
                zipOutputStream.closeEntry();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void fromZip(File source, File target) {
        try (ZipFile zipFile = new ZipFile(source)) {
            zipFile.stream().parallel().forEach(e -> {
                if (e.isDirectory()) {
                    return;
                }
                try {
                    Files.toFile(zipFile.getInputStream(e), Files.file(target, e.getName()));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static Class getCaller(Class<?> obj) {
        try {
            return Class.forName(Thread.currentThread().getStackTrace()[3].getClassName(), false, obj.getClassLoader());
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    public interface ReadHandle {

        void read(BufferedReader reader) throws IOException;
    }

    public interface WriteHandle {

        void write(BufferedWriter writer) throws IOException;
    }
}
