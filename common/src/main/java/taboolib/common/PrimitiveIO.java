package taboolib.common;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * TabooLib
 * taboolib.common.env.IO
 *
 * @author 坏黑
 * @since 2023/3/31 14:59
 */
@SuppressWarnings("CallToPrintStackTrace")
public class PrimitiveIO {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private static final int BUFFER_SIZE = 8192;
    private static final ThreadLocal<MessageDigest> DIGEST_THREAD_LOCAL = ThreadLocal.withInitial(new Supplier<MessageDigest>() {

        @Override
        public MessageDigest get() {
            try {
                return MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    });

    /**
     * 当前运行的文件名
     **/
    private static String runningFileName = "TabooLib";

    /**
     * 是否再用 Logger 输出
     **/
    private static boolean useJavaLogger = false;

    static {
        // 获取插件文件
        try {
            runningFileName = new File(PrimitiveIO.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
            // 如果这个玩意叫 Common
            if (runningFileName.startsWith("common-")) {
                runningFileName = "App";
            }
        } catch (Throwable ignored) {
        }
        // 检查 Paper 核心控制台拦截工具
        try {
            Class.forName("io.papermc.paper.logging.SysoutCatcher");
            useJavaLogger = true;
        } catch (ClassNotFoundException ignored) {
        }
        // 检查 BungeeCord 环境
        try {
            Class.forName("net.md_5.bungee.BungeeCord");
            useJavaLogger = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * 开发模式输出
     */
    public static void dev(Object message, Object... args) {
        if (PrimitiveSettings.IS_DEV_MODE) {
            println("[DEV] " + message, args);
        }
    }

    /**
     * 调试模式输出
     */
    public static void debug(Object message, Object... args) {
        if (PrimitiveSettings.IS_DEBUG_MODE) {
            println("[DEBUG] " + message, args);
        }
    }

    /**
     * 控制台输出
     */
    public static void println(Object message, Object... args) {
        if (useJavaLogger) {
            Logger.getLogger(runningFileName).info(String.format(Objects.toString(message), args));
        } else {
            System.out.printf(message + "%n", args);
        }
    }

    /**
     * 控制台输出
     */
    public static void error(Object message, Object... args) {
        if (useJavaLogger) {
            Logger.getLogger(runningFileName).severe(String.format(Objects.toString(message), args));
        } else {
            System.err.printf(message + "%n", args);
        }
    }

    /**
     * 验证文件完整性
     *
     * @param file     文件
     * @param hashFile 哈希文件
     */
    public static boolean validation(File file, File hashFile) {
        return file.exists() && hashFile.exists() && PrimitiveIO.readFile(hashFile).startsWith(PrimitiveIO.getHash(file));
    }

    /**
     * 获取文件哈希，使用 sha-1 算法
     */
    @NotNull
    public static String getHash(File file) {
        MessageDigest digest = DIGEST_THREAD_LOCAL.get();
        digest.reset(); // Ensure the MessageDigest is reset before each use
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int total;
            while ((total = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, total);
            }
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    /**
     * 获取字符串哈希
     */
    public static String getHash(String data) {
        MessageDigest digest = DIGEST_THREAD_LOCAL.get();
        digest.reset(); // Ensure the MessageDigest is reset before each use
        digest.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 读取文件内容
     */
    @NotNull
    public static String readFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return readFully(fileInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    /**
     * 从 InputStream 读取全部内容
     *
     * @param inputStream 输入流
     * @param charset     编码
     */
    @NotNull
    public static String readFully(InputStream inputStream, Charset charset) throws IOException {
        return new String(readFully(inputStream), charset);
    }

    /**
     * 从 InputStream 读取全部内容
     *
     * @param inputStream 输入流
     */
    public static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    /**
     * 通过 FileChannel 复制文件
     */
    @NotNull
    public static File copyFile(File from, File to) {
        try (FileInputStream fileIn = new FileInputStream(from); FileOutputStream fileOut = new FileOutputStream(to); FileChannel channelIn = fileIn.getChannel(); FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
        return to;
    }

    /**
     * 下载文件
     *
     * @param url 地址
     * @param out 目标文件
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static void downloadFile(URL url, File out) throws IOException {
        out.getParentFile().mkdirs();
        InputStream ins = url.openStream();
        OutputStream outs = Files.newOutputStream(out.toPath());
        byte[] buffer = new byte[BUFFER_SIZE];
        for (int len; (len = ins.read(buffer)) > 0; outs.write(buffer, 0, len))
            ;
        outs.close();
        ins.close();
    }

    public static String getRunningFileName() {
        return runningFileName;
    }
}
