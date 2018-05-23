package me.skymc.taboolib.fileutils;

import ch.njol.util.Closeable;
import com.ilummc.tlib.util.IO;
import javafx.print.PageLayout;
import me.skymc.taboolib.Main;
import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;

public class FileUtils {

    public static String ip() {
        URL url;
        URLConnection con;
        try {
            url = new URL("http://1212.ip138.com/ic.asp");
            con = url.openConnection();
        } catch (Exception ignored) {
            return "[IP ERROR]";
        }
        InputStream ins = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            ins = con.getInputStream();
            inputStreamReader = new InputStreamReader(ins, "GB2312");
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder webContent = new StringBuilder();
            bufferedReader.lines().forEach(webContent::append);
            int start = webContent.indexOf("[") + 1;
            int end = webContent.indexOf("]");
            return webContent.substring(start, end);
        } catch (Exception ignored) {
            return "[IP ERROR]";
        } finally {
            IOUtils.close(con);
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(ins);
        }
    }

    public static InputStream getResource(String filename) {
        return getResource(Main.getInst(), filename);
    }

    public static InputStream getResource(Plugin plugin, String filename) {
        try {
            URL url = plugin.getClass().getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    public static void inputStreamToFile(InputStream inputStream, File file) {
        try {
            String text = new String(IO.readFully(inputStream), Charset.forName("utf-8"));
            FileWriter fileWriter = new FileWriter(FileUtils.createNewFile(file));
            fileWriter.write(text);
            fileWriter.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * 检测文件并创建
     *
     * @param file 文件
     */
    public static File createNewFile(File file) {
        if (file != null && !file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception ignored) {
            }
        }
        return file;
    }

    /**
     * 创建并获取文件
     *
     * @param Path     目录
     * @param filePath 地址
     * @return
     */
    public static File file(File Path, String filePath) {
        return createNewFile(new File(Path, filePath));
    }

    /**
     * 创建并获取文件
     *
     * @param filePath 地址
     * @return {@link File}
     */
    public static File file(String filePath) {
        return createNewFile(new File(filePath));
    }

    /**
     * 删除文件夹
     *
     * @param file 文件夹
     */
    public void deleteAllFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        for (File file1 : Objects.requireNonNull(file.listFiles())) {
            deleteAllFile(file1);
        }
        file.delete();
    }

    /**
     * 复制文件夹
     *
     * @param file1 文件1
     * @param file2 文件2
     */
    public void copyAllFile(String file1, String file2) {
        File _file1 = new File(file1);
        File _file2 = new File(file2);
        if (!_file2.exists()) {
            if (!_file1.isDirectory()) {
                createNewFile(_file2);
            } else {
                _file2.mkdirs();
            }
        }
        if (_file1.isDirectory()) {
            for (File file : Objects.requireNonNull(_file1.listFiles())) {
                if (file.isDirectory()) {
                    copyAllFile(file.getAbsolutePath(), file2 + "/" + file.getName());
                } else {
                    fileChannelCopy(file, new File(file2 + "/" + file.getName()));
                }
            }
        } else {
            fileChannelCopy(_file1, _file2);
        }
    }

    /**
     * 复制文件（通道）
     *
     * @param file1 文件1
     * @param file2 文件2
     */
    public void fileChannelCopy(File file1, File file2) {
        FileInputStream fileIn = null;
        FileOutputStream fileOut = null;
        FileChannel channelIn = null;
        FileChannel channelOut = null;
        try {
            fileIn = new FileInputStream(file1);
            fileOut = new FileOutputStream(file2);
            channelIn = fileIn.getChannel();
            channelOut = fileOut.getChannel();
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException ignored) {
        } finally {
            IOUtils.closeQuietly(channelIn);
            IOUtils.closeQuietly(channelOut);
            IOUtils.closeQuietly(fileIn);
            IOUtils.closeQuietly(fileOut);
        }
    }

    /**
     * 通过输入流读取文本
     *
     * @param in     输入流
     * @param size   大小
     * @param encode 编码
     * @return 文本
     */
    public static String getStringFromInputStream(InputStream in, int size, String encode) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[size];
            int i;
            while ((i = in.read(b)) > 0) {
                bos.write(b, 0, i);
            }
            return new String(bos.toByteArray(), encode);
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * 通过文件读取文本
     *
     * @param file   文件
     * @param size   大小
     * @param encode 编码
     * @return 文本
     */
    public static String getStringFromFile(File file, int size, String encode) {
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        try {
            fin = new FileInputStream(file);
            bin = new BufferedInputStream(fin);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[size];
            int i;
            while ((i = bin.read(b)) > 0) {
                bos.write(b, 0, i);
            }
            return new String(bos.toByteArray(), encode);
        } catch (IOException ignored) {
        } finally {
            IOUtils.closeQuietly(bin);
            IOUtils.closeQuietly(fin);
        }
        return null;
    }

    /**
     * 通过 URL 读取文本
     *
     * @param url 地址
     * @param def 默认值
     * @return 文本
     */
    public static String getStringFromURL(String url, String def) {
        String s = getStringFromURL(url, 1024);
        return s == null ? def : s;
    }

    /**
     * 通过 URL 读取文本
     *
     * @param url  地址
     * @param size 大小
     * @return 文本
     */
    public static String getStringFromURL(String url, int size) {
        URLConnection conn = null;
        BufferedInputStream bin = null;
        try {
            conn = new URL(url).openConnection();
            bin = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[size];
            int i;
            while ((i = bin.read(b)) > 0) {
                bos.write(b, 0, i);
            }
            return new String(bos.toByteArray(), conn.getContentEncoding() == null ? "UTF-8" : conn.getContentEncoding());
        } catch (IOException ignored) {
        } finally {
            IOUtils.close(conn);
            IOUtils.closeQuietly(bin);
        }
        return null;
    }

    /**
     * 下载文件
     *
     * @param downloadURL 下载地址
     * @param file        保存位置
     */
    public static void download(String downloadURL, File file) {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(downloadURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");

            inputStream = conn.getInputStream();
            byte[] data = read(inputStream);

            fos = new FileOutputStream(createNewFile(file));
            fos.write(data);
        } catch (Exception ignored) {
        } finally {
            IOUtils.close(conn);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Deprecated
    public static void download(String downloadURL, String filename, File saveDir) {
        download(downloadURL, new File(saveDir, filename));
    }

    public static byte[] read(InputStream in) {
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while ((len = in.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch (Exception ignored) {
        }
        return bos.toByteArray();
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignored) {
        }
    }
}
