package io.izzel.taboolib.util;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.module.db.local.SecuredFile;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.util.plugin.PluginUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
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

/**
 * @author sky
 */
public class Files {

    public static List<Class> getClasses(Plugin plugin) {
        return getClasses(plugin, new String[0]);
    }

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

    @TSchedule(period = 100, async = true)
    public static void clearTempFiles() {
        deepDelete(new File("plugins/TabooLib/temp"));
    }

    public static InputStream getResource(String filename) {
        return getResource(TabooLib.getPlugin(), filename);
    }

    public static InputStream getResource(Plugin plugin, String filename) {
        return plugin instanceof InternalPlugin ? getTabooLibResource(filename) : plugin.getClass().getClassLoader().getResourceAsStream(filename);
    }

    public static InputStream getResourceChecked(Plugin plugin, String filename) {
        return plugin instanceof InternalPlugin ? getResource(plugin, "__resources__/" + filename) : getResource(filename);
    }

    public static InputStream getTabooLibResource(String filename) {
        return getCanonicalResource(TabooLib.getPlugin(), filename);
    }

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

    public static File releaseResource(Plugin plugin, String path) {
        releaseResource(plugin, path, false);
        return new File(plugin.getDataFolder(), path);
    }

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

    public static File toFile(String in, File file) {
        try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(in);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return file;
    }

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

    public static boolean downloadFile(String in, File file) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            toFile(bufferedInputStream, file);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    public static File file(File path, String filePath) {
        return file(new File(path, filePath));
    }

    public static File file(String filePath) {
        return file(new File(filePath));
    }

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

    public static File folder(File path, String filePath) {
        return folder(new File(path, filePath));
    }

    public static File folder(String filePath) {
        return folder(new File(filePath));
    }

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

    public static void copy(File file1, File file2) {
        try (FileInputStream fileIn = new FileInputStream(file1);
             FileOutputStream fileOut = new FileOutputStream(file2);
             FileChannel channelIn = fileIn.getChannel();
             FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
    }

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
            for (File file : Objects.requireNonNull(originFile.listFiles())) {
                if (file.isDirectory()) {
                    deepCopy(file.getAbsolutePath(), targetFileName + "/" + file.getName());
                } else {
                    copy(file, new File(targetFileName + "/" + file.getName()));
                }
            }
        } else {
            copy(originFile, targetFile);
        }
    }

    public static void deepDelete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        for (File file1 : Objects.requireNonNull(file.listFiles())) {
            deepDelete(file1);
        }
        file.delete();
    }

    public static String readFromURL(String url, String def) {
        return Optional.ofNullable(readFromURL(url)).orElse(def);
    }

    public static String readFromURL(String url, Charset charset, String def) {
        return Optional.ofNullable(readFromURL(url, charset)).orElse(def);
    }

    public static String readFromURL(String url) {
        return readFromURL(url, StandardCharsets.UTF_8);
    }

    public static String readFromURL(String url, Charset charset) {
        try (InputStream inputStream = new URL(url).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return new String(IO.readFully(bufferedInputStream), charset);
        } catch (UnknownHostException ignored) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static String readFromFile(File file) {
        return readFromFile(file, 1024, StandardCharsets.UTF_8);
    }

    public static String readFromFile(File file, int size) {
        return readFromFile(file, size, StandardCharsets.UTF_8);
    }

    public static String readFromFile(File file, int size, Charset encode) {
        try (FileInputStream fin = new FileInputStream(file); BufferedInputStream bin = new BufferedInputStream(fin)) {
            return readFromStream(fin, size, encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> readToList(File file) {
        try (FileInputStream fin = new FileInputStream(file); InputStreamReader isr = new InputStreamReader(fin, StandardCharsets.UTF_8); BufferedReader bin = new BufferedReader(isr)) {
            return bin.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<String> readToList(InputStream inputStream) {
        try (InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8); BufferedReader bin = new BufferedReader(isr)) {
            return bin.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static String readFromStream(InputStream in) {
        return readFromStream(in, 1024, StandardCharsets.UTF_8);
    }

    public static String readFromStream(InputStream in, int size) {
        return readFromStream(in, size, StandardCharsets.UTF_8);
    }

    public static String readFromStream(InputStream in, int size, Charset encode) {
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

    public static void read(File file, ReadHandle readHandle) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            readHandle.read(bufferedReader);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void read(InputStream in, ReadHandle readHandle) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            readHandle.read(bufferedReader);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void write(File file, WriteHandle writeHandle) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            writeHandle.write(bufferedWriter);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void writeAppend(File file, WriteHandle writeHandle) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            writeHandle.write(bufferedWriter);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void write(OutputStream out, WriteHandle writeHandle) {
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            writeHandle.write(bufferedWriter);
            bufferedWriter.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String encodeYAML(FileConfiguration file) {
        return Base64Coder.encodeLines(file.saveToString().getBytes()).replaceAll("\\s+", "");
    }

    public static FileConfiguration decodeYAML(String args) {
        return SecuredFile.loadConfiguration(Base64Coder.decodeString(args));
    }

    public static FileConfiguration load(File file) {
        return loadYaml(file);
    }

    public static YamlConfiguration loadYaml(File file) {
        return SecuredFile.loadConfiguration(file);
    }

    public static String getFileHash(File file, String algorithm) {
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
