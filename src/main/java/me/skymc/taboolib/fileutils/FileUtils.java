package me.skymc.taboolib.fileutils;

import ch.njol.util.Closeable;
import me.skymc.taboolib.message.MsgUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

public class FileUtils {

    public static String ip() {
        try {
            InputStream ins = null;
            URL url = new URL("http://1212.ip138.com/ic.asp");
            URLConnection con = url.openConnection();
            ins = con.getInputStream();
            InputStreamReader isReader = new InputStreamReader(ins, "GB2312");
            BufferedReader bReader = new BufferedReader(isReader);
            StringBuilder webContent = new StringBuilder();
            String str = null;
            while ((str = bReader.readLine()) != null) {
                webContent.append(str);
            }
            int start = webContent.indexOf("[") + 1;
            int end = webContent.indexOf("]");
            ins.close();
            return webContent.substring(start, end);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "[IP ERROR]";
    }

    /**
     * 创建并获取文件
     *
     * @param filePath
     * @return
     */
    public static File file(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }
        return file;
    }

    /**
     * 创建并获取文件
     *
     * @param Path
     * @param filePath
     * @return
     */
    public static File file(File Path, String filePath) {
        File file = new File(Path, filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }
        return file;
    }

    /**
     * 删除文件夹
     *
     * @param file
     */
    public void deleteAllFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for (File file1 : files) {
            deleteAllFile(file1);
        }
        file.delete();
    }

    /**
     * 复制文件夹
     *
     * @param file1 文件1
     * @param file2 文件2
     * @throws Exception
     */
    public void copyAllFile(String file1, String file2) throws Exception {
        File _file1 = new File(file1);
        File _file2 = new File(file2);
        if (!_file2.exists()) {
            if (!_file1.isDirectory()) {
                _file2.createNewFile();
            } else {
                _file2.mkdirs();
            }
        }
        if (_file1.isDirectory()) {
            for (File file : _file1.listFiles()) {
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
        } catch (Exception e) {
            //
        } finally {
            try {
                fileIn.close();
                channelIn.close();
                fileOut.close();
                channelOut.close();
            } catch (Exception e) {
                //
            }
        }
    }

    /**
     * 通过输入流读取文本
     *
     * @param in
     * @param size
     * @param encode
     * @return
     */
    public static String getStringFromInputStream(InputStream in, int size, String encode) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] b = new byte[size];
            int i = 0;

            while ((i = in.read(b)) > 0) {
                bos.write(b, 0, i);
            }

            bos.close();
            return new String(bos.toByteArray(), encode);
        } catch (IOException e) {
            MsgUtils.warn("输入流读取出错: &4" + e.getMessage());
        }
        return null;
    }

    /**
     * 通过文件读取文本
     *
     * @param file
     * @param size
     * @param encode
     * @return
     */
    public static String getStringFromFile(File file, int size, String encode) {
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] b = new byte[size];
            int i = 0;

            while ((i = bin.read(b)) > 0) {
                bos.write(b, 0, i);
            }

            bos.close();
            bin.close();
            fin.close();
            return new String(bos.toByteArray(), encode);
        } catch (IOException e) {
            MsgUtils.warn("文件读取出错: &4" + e.getMessage());
        }
        return null;
    }

    /**
     * 通过 URL 读取文本
     *
     * @param url
     * @param size
     * @return
     */
    public static String getStringFromURL(String url, int size) {
        try {
            URLConnection conn = new URL(url).openConnection();
            BufferedInputStream bin = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] b = new byte[size];
            int i = 0;

            while ((i = bin.read(b)) > 0) {
                bos.write(b, 0, i);
            }

            bos.close();
            bin.close();
            return new String(bos.toByteArray(), conn.getContentEncoding() == null ? "UTF-8" : conn.getContentEncoding());
        } catch (IOException e) {
            MsgUtils.warn("网络访问出错: &4" + e.getMessage());
        }
        return null;
    }

    public static String getStringFromURL(String url, String def) {
        String s = getStringFromURL(url, 1024);
        return s == null ? def : s;
    }

    /**
     * 下载文件
     *
     * @param urlStr
     * @param filename
     * @param saveDir
     */
    public static void download(String urlStr, String filename, File saveDir) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 超时时间
            conn.setConnectTimeout(5 * 1000);
            // 防止屏蔽程序抓取而返回 403 错误
            conn.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");

            // 得到输入流
            InputStream inputStream = conn.getInputStream();
            // 获取数组
            byte[] data = read(inputStream);

            // 创建文件夹
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            // 保存文件
            File file = new File(saveDir, filename);
            FileOutputStream fos = new FileOutputStream(file);

            // 写入文件
            fos.write(data);

            // 结束
            fos.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] read(InputStream in) {
        byte[] buffer = new byte[1024];
        int len = 0;
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
