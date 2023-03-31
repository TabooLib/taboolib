package taboolib.common.env;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * TabooLib
 * taboolib.common.env.IO
 *
 * @author 坏黑
 * @since 2023/3/31 14:59
 */
public class IO {

    public static boolean validation(File file, File hashFile) {
        return file.exists() && hashFile.exists() && IO.readFile(hashFile).startsWith(IO.getHash(file));
    }

    @NotNull
    public static String getHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-1");
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                byte[] buffer = new byte[1024];
                int total;
                while ((total = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, total);
                }
            }
            return getHash(digest);
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    @NotNull
    public static String getHash(MessageDigest digest) {
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @NotNull
    public static String readFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return readFully(fileInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    @NotNull
    public static String readFully(InputStream inputStream, Charset charset) throws IOException {
        return new String(readFully(inputStream), charset);
    }

    @NotNull
    public static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    @NotNull
    public static File copyFile(File file1, File file2) {
        try (FileInputStream fileIn = new FileInputStream(file1); FileOutputStream fileOut = new FileOutputStream(file2); FileChannel channelIn = fileIn.getChannel(); FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
        return file2;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static void downloadFile(URL url, File out) throws IOException {
        InputStream ins = url.openStream();
        OutputStream outs = Files.newOutputStream(out.toPath());
        byte[] buffer = new byte[4096];
        for (int len; (len = ins.read(buffer)) > 0; outs.write(buffer, 0, len))
            ;
        outs.close();
        ins.close();
    }
}
