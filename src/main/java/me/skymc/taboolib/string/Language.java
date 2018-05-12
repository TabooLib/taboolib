package me.skymc.taboolib.string;

import com.google.common.base.Charsets;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Deprecated
public class Language {

    private FileConfiguration conf = null;
    private String langName;
    private Plugin plugin;

    public Language(Plugin plugin) {
        this("zh_CN", plugin, false);
    }

    public Language(String name, Plugin plugin) {
        this(name, plugin, false);
    }

    public Language(String name, Plugin plugin, boolean utf8) {
        this.plugin = plugin;
        this.langName = name;

        File file = new File(getLanguageDir(), name + ".yml");
        if (!file.exists()) {
            plugin.saveResource("Language/" + name + ".yml", true);
        }

        if (utf8) {
            reloadUTF8(this.langName);
        } else {
            reload(this.langName);
        }
    }

    public FileConfiguration getConfiguration() {
        return conf;
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(Player player, String key) {
        player.sendMessage(get(key));
    }

    public void sendList(CommandSender sender, String key) {
        List<String> list = getList(key);
        for (String msg : list) {
            sender.sendMessage(msg);
        }
    }

    public void sendList(Player player, String key) {
        List<String> list = getList(key);
        for (String msg : list) {
            player.sendMessage(msg);
        }
    }

    public String get(String key) {
        if (conf == null || conf.getString(key) == null) {
            return "§4[Language \"" + key + "\" Not Found]";
        }
        return conf.getString(key).replace("&", "§");
    }

    public List<String> getList(String key) {
        if (conf == null || conf.getString(key) == null) {
            return Collections.singletonList("§4[Language \"" + key + "\" Not Found]");
        }
        List<String> list = conf.getStringList(key);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).replace("&", "§"));
        }
        return list;
    }

    public void reload() {
        reload(langName);
    }

    public void reload(String name) {
        File langFile = new File(getLanguageDir(), name + ".yml");
        if (!langFile.exists()) {
            MsgUtils.warn("语言文件 " + langName + " 不存在, 请更改配置文件");
            return;
        }
        conf = YamlConfiguration.loadConfiguration(langFile);
    }

    public void reloadUTF8(String name) {
        File langFile = new File(getLanguageDir(), name + ".yml");
        if (!langFile.exists()) {
            MsgUtils.warn("语言文件 " + langName + " 不存在, 请更改配置文件");
            return;
        }
        try {
            conf = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(langFile), Charsets.UTF_8));
        } catch (FileNotFoundException e) {
            conf = new YamlConfiguration();
        }
    }

    private File getLanguageDir() {
        File dir = new File(plugin.getDataFolder(), "Language");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}