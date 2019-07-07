import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.izzel.taboolib.PluginLoader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipFile;

/**
 * @Author 坏黑
 * @Since 2019-07-05 9:03
 */
public abstract class Plugin extends JavaPlugin {

    /**
     * 版本信息获取地址
     * 优先采用国内地址
     * 防止部分机器封禁海外访问
     */
    public static final String[][] URL = {
            {
                    "https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/latest.json",
                    "https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib.jar"
            },
            {
                    "https://api.github.com/repos/Bkm016/TabooLib/releases/latest",
                    "https://github.com/Bkm016/TabooLib/releases/latest/download/TabooLib.jar",
            },
    };

    protected static Plugin plugin;
    protected static File libFile = new File("TabooLib.jar");

    /**
     * 插件在初始化过程中出现错误
     * 将在 onLoad 方法下关闭插件
     */
    protected static boolean initFailed;

    static {
        init();
    }

    @Override
    public final void onLoad() {
        if (initFailed) {
            setEnabled(false);
            return;
        }
        plugin = this;
        PluginLoader.addPlugin(this);
        PluginLoader.load(this);
        try {
            onLoading();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public final void onEnable() {
        if (initFailed) {
            return;
        }
        PluginLoader.start(this);
        try {
            onStarting();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Bukkit.getScheduler().runTask(this, () -> {
            PluginLoader.active(this);
            onActivated();
        });
    }

    @Override
    public final void onDisable() {
        if (initFailed) {
            return;
        }
        PluginLoader.stop(this);
        try {
            onStopping();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 代替 JavaPlugin 本身的 onLoad 方法
     */
    public void onLoading() {
    }

    /**
     * 代替 JavaPlugin 本身的 onEnable 方法
     */
    public void onStarting() {
    }

    /**
     * 代替 JavaPlugin 本身的 onDisable 方法
     */
    public void onStopping() {
    }

    /**
     * 当服务端完全启动时执行该方法
     * 完全启动指 "控制台可以输入命令且得到反馈时"
     * <p>
     * 使用 @TSchedule 同样可以代替该方法
     */
    public void onActivated() {
    }

    /**
     * 检查 TabooLib 是否已经被载入
     * 跳过 TabooLib 主类的初始化过程
     */
    public static boolean isLoaded() {
        try {
            return Class.forName("io.izzel.taboolib.TabooLib", false, Bukkit.class.getClassLoader()) != null;
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * 获取 TabooLib 当前运行版本
     */
    public static double getVersion() {
        try {
            ZipFile zipFile = new ZipFile(libFile);
            return NumberConversions.toDouble(readFully(zipFile.getInputStream(zipFile.getEntry("version")), StandardCharsets.UTF_8));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取 TabooLib 当前最新版本
     * 获取内容为版本号+访问地址+下载地址
     */
    public static String[] getNewVersion() {
        for (String[] url : URL) {
            String read = readFromURL(url[0]);
            if (read == null) {
                continue;
            }
            try {
                JsonObject jsonObject = (JsonObject) new JsonParser().parse(read);
                if (jsonObject.has("tag_name")) {
                    return new String[] {jsonObject.get("tag_name").getAsString(), url[0], url[1]};
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将文件读取至内存中
     * 读取后不会随着插件的卸载而卸载
     * 请在执行前判断是否已经被读取
     * 防止出现未知错误
     */
    private static void addToPath(File file) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(Bukkit.class.getClassLoader(), file.toURI().toURL());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static boolean downloadFile(String in, File file) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            toFile(bufferedInputStream, file);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    private static File toFile(String in, File file) {
        try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(in);
            bufferedWriter.flush();
        } catch (Exception ignored) {
        }
        return file;
    }

    private static File toFile(InputStream inputStream, File file) {
        try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            bos.flush();
        } catch (Exception ignored) {
        }
        return file;
    }

    private static String readFromURL(String in, String def) {
        return Optional.ofNullable(readFromURL(in)).orElse(def);
    }

    private static String readFromURL(String in) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return new String(readFully(bufferedInputStream));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private static String readFully(InputStream inputStream, Charset charset) throws IOException {
        return new String(readFully(inputStream), charset);
    }

    private static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    private static Class<?> getMainClass() {
        File file = file(new File("plugins/TabooLib/temp/" + UUID.randomUUID()));
        try {
            ZipFile zipFile = new ZipFile(toFile(Plugin.class.getProtectionDomain().getCodeSource().getLocation().openStream(), file));
            return Class.forName(YamlConfiguration.loadConfiguration(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("plugin.yml")))).getString("main"));
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

    private static File file(File file) {
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

    private static File folder(File file) {
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

    private static void deepDelete(File file) {
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

    private static void init() {
        // 检查 TabooLib 文件是否存在
        if (!libFile.exists()) {
            // 本地资源检测
            InputStream resourceAsStream = Plugin.class.getClassLoader().getResourceAsStream("TabooLib.jar");
            if (resourceAsStream != null) {
                // 写入文件
                toFile(resourceAsStream, libFile);
            }
            // 在线资源下载
            else if (!download()) {
                return;
            }
        }
        // 检查 TabooLib 运行版本是否正常
        else {
            double version = getVersion();
            // 本地版本获取失败
            if (version == -1) {
                // 重新下载文件，如果下载失败则停止加载
                if (!download()) {
                    return;
                }
            }
            Class<?> mainClass = getMainClass();
            if (mainClass != null && mainClass.isAnnotationPresent(Version.class)) {
                double requireVersion = mainClass.getAnnotation(Version.class).value();
                // 依赖版本高于当前运行版本
                if (requireVersion > version) {
                    // 获取版本信息
                    String[] newVersion = getNewVersion();
                    if (newVersion == null) {
                        close();
                        return;
                    }
                    // 检查依赖版本是否合理
                    // 如果插件使用不合理的版本则跳过下载防止死循环
                    // 并跳过插件加载
                    if (requireVersion > NumberConversions.toInt(newVersion[0])) {
                        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c版本 §4" + requireVersion + " §c无法获取.");
                        initFailed = true;
                        return;
                    }
                    Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7正在重新下载资源文件.");
                    if (downloadFile(newVersion[2], libFile)) {
                        // 如果资源下载成功则重启服务器
                        restart();
                    }
                    return;
                }
            }
        }
        // 检查 TabooLib 是否已经被加载
        if (!isLoaded()) {
            // 将 TabooLib 通过 Bukkit.class 类加载器加载至内存中供其他插件使用
            // 并保证在热重载过程中不会被 Bukkit 卸载
            addToPath(libFile);
            // 初始化 TabooLib 主类
            try {
                Class.forName("io.izzel.taboolib.TabooLib");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // 清理临时文件
        deepDelete(new File("plugins/TabooLib/temp"));
    }

    private static boolean download() {
        Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7正在下载资源文件.");
        String[] newVersion = getNewVersion();
        if (newVersion == null || !downloadFile(newVersion[2], libFile)) {
            close();
            return false;
        }
        return true;
    }

    private static void close() {
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c#################### 错误 ####################");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  初始化 §4TabooLib §c失败!");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  无法获取版本信息或下载时出现错误.");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  请检查网络连接.");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  服务端将在 5 秒后继续启动.");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c#################### 错误 ####################");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        try {
            Thread.sleep(5000L);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        initFailed = true;
    }

    private static void restart() {
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c#################### 警告 ####################");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  初始化 §4TabooLib §c失败!");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  当前运行的版本低于插件所需版本.");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  已下载最新版.");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c  服务端将在 5 秒后重新启动.");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c#################### 警告 ####################");
        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c");
        try {
            Thread.sleep(5000L);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        initFailed = true;
        Bukkit.shutdown();
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static File getLibFile() {
        return libFile;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Version {

        double value();

    }
}
