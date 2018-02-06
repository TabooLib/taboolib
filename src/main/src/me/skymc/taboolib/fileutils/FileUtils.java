package me.skymc.taboolib.fileutils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import me.skymc.taboolib.client.LogClient;
import me.skymc.taboolib.methods.MethodsUtils;

public class FileUtils {
	
	public static String ip() {  
        try {  
        	InputStream ins = null;  
            URL url = new URL("http://1212.ip138.com/ic.asp");  
            URLConnection con = url.openConnection();  
            ins = con.getInputStream();  
            InputStreamReader isReader = new InputStreamReader(ins, "GB2312");  
            BufferedReader bReader = new BufferedReader(isReader);  
            StringBuffer webContent = new StringBuffer();  
            String str = null;  
            while ((str = bReader.readLine()) != null) {  
                webContent.append(str);  
            }  
            int start = webContent.indexOf("[") + 1;  
            int end = webContent.indexOf("]");  
            ins.close();
            return webContent.substring(start, end);  
        } 
        catch (Exception e) {
			// TODO: handle exception
		}
        return "[IP ERROR]";
    }  
	
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
			e.printStackTrace();
		}
		return null;
	}
	
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
			e.printStackTrace();
		}
		return null;
	}
	
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
			e.printStackTrace();
		}
		return null;
	}
	
    /**
     * Write a UTF8 file
     *
     * @param strs list of lines
     * @param f file to write
     *
     */
    public FileUtils(List<String> strs, File f) throws IOException 
    {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF8"));
        for(String s : strs) {
            out.write(s+"\n");
        }
        out.flush();
        out.close();
    }

    public FileUtils(String str, File f) throws IOException 
    {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF8"));
        out.write(str);
        out.flush();
        out.close();
    }
    
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static byte[] read(InputStream in) {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			while((len = in.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			bos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
}
