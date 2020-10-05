package io.izzel.taboolib.module.config;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.local.SecuredFile;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Pair;
import io.izzel.taboolib.util.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * @Author sky
 * @Since 2020-04-27 21:02
 */
public class TConfigMigrate {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public static List<String> migrate(InputStream current, InputStream source) {
        boolean migrated = false;
        List<String> content = Files.readToList(current);
        String cc = String.join("\n", content);
        String cs = String.join("\n", Files.readToList(source));
        SecuredFile c = SecuredFile.loadConfiguration(cc);
        SecuredFile s = SecuredFile.loadConfiguration(cs);
        String hash1 = Strings.hashKeyForDisk(cs, "sha-1");
        String hash2 = "";
        for (String line : content) {
            if (line.startsWith("# HASH ")) {
                hash2 = line.substring("# HASH ".length()).trim().split(" ")[0];
                break;
            }
        }
        if (Objects.equals(hash1, hash2)) {
            return null;
        }
        List<Pair<String, String[]>> update = Lists.newArrayList();
        List<Pair<String, Object>> contrast = contrast(c.getValues(true), s.getValues(true));
        for (Pair<String, Object> pair : contrast) {
            int index = pair.getKey().lastIndexOf(".");
            if (pair.getValue() == null) {
                Object data = s.get(pair.getKey());
                String[] nodes = pair.getKey().split("\\.");
                int regex = 0;
                int match = 0;
                int find = -1;
                for (int i = 0; i < content.size(); i++) {
                    String line = content.get(i);
                    for (int j = regex; j < nodes.length; j++) {
                        if (line.matches("( *)(['\"])?(" + nodes[j] + ")(['\"])?:(.*)")) {
                            match++;
                            find = i;
                            regex = j;
                            break;
                        }
                    }
                }
                if (find == -1) {
                    update.add(new Pair<>(pair.getKey(), SecuredFile.dump(data).split("\n")));
                } else {
                    String space = Strings.copy("  ", nodes.length - 1);
                    String[] dumpList = SecuredFile.dump(data).split("\n");
                    if (dumpList.length > 1) {
                        IntStream.range(0, dumpList.length).forEach(j -> dumpList[j] = space + "  # " + dumpList[j]);
                    }
                    ArrayUtil.addAutoExpand(content, find + 1, space + "# ------------------------- #\n" + space + "#  UPDATE " + dateFormat.format(System.currentTimeMillis()) + "  #\n" + space + "# ------------------------- #", "");
                    if (dumpList.length == 1) {
                        ArrayUtil.addAutoExpand(content, find + 2, space + "# " + pair.getKey().substring(index + 1) + ": " + dumpList[0] + "\n", "");
                    } else {
                        ArrayUtil.addAutoExpand(content, find + 2, space + "# " + pair.getKey().substring(index + 1) + ":", "");
                        ArrayUtil.addAutoExpand(content, find + 3, String.join("\n# ", dumpList) + "\n", "");
                    }
                    migrated = true;
                }
            }
        }
        if (update.size() > 0) {
            content.add("");
            content.add("# ------------------------- #\n" + "#  UPDATE " + dateFormat.format(System.currentTimeMillis()) + "  #\n" + "# ------------------------- #");
            for (Pair<String, String[]> pair : update) {
                if (pair.getValue().length == 1) {
                    content.add("# " + pair.getKey() + ": " + pair.getValue()[0]);
                } else {
                    content.add("# " + pair.getKey() + ":");
                    content.add(String.join("\n# ", pair.getValue()));
                }
            }
            migrated = true;
        }
        if (migrated) {
            if (hash2.isEmpty()) {
                content.add("");
                content.add("# --------------------------------------------- #");
                content.add("# HASH " + hash1 + " #");
                content.add("# --------------------------------------------- #");
            } else {
                for (int i = 0; i < content.size(); i++) {
                    String line = content.get(i);
                    if (line.startsWith("# HASH ")) {
                        content.set(i, "# HASH " + hash1 + " #");
                        break;
                    }
                }
            }
        }
        return migrated ? content : null;
    }

    public static Map<String, Object> toMap(FileConfiguration conf) {
        Map<String, Object> map = conf.getValues(true);
        map.entrySet().removeIf(next -> next.getValue() instanceof ConfigurationSection);
        return map;
    }

    public static List<Pair<String, Object>> contrast(Map<?, ?> current, Map<?, ?> source) {
        List<String> deleted = Lists.newArrayList();
        List<Pair<String, Object>> difference = Lists.newArrayList();
        // change & add
        for (Map.Entry<?, ?> entry : current.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                continue;
            }
            if (entry.getValue() instanceof Map && source.get(entry.getKey()) instanceof Map) {
                List<Pair<String, Object>> contrast = contrast((Map<?, ?>) entry.getValue(), (Map<?, ?>) source.get(entry.getKey()));
                for (Pair<String, Object> pair : contrast) {
                    pair.setKey(entry.getKey() + "." + pair.getKey());
                }
                difference.addAll(contrast);
            } else if (!Objects.equals(entry.getValue(), source.get(entry.getKey()))) {
                difference.add(new Pair<>(entry.getKey().toString(), entry.getValue()));
            }
        }
        // delete
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (deleted.stream().anyMatch(delete -> entry.getKey().toString().startsWith(delete) && delete.split("\\.").length < entry.getKey().toString().split("\\.").length)) {
                continue;
            }
            if (entry.getValue() instanceof Map && current.get(entry.getKey()) instanceof Map) {
                List<Pair<String, Object>> contrast = contrast((Map<?, ?>) entry.getValue(), (Map<?, ?>) source.get(entry.getKey()));
                for (Pair<String, Object> pair : contrast) {
                    pair.setKey(entry.getKey() + "." + pair.getKey());
                }
                difference.addAll(contrast);
            } else if (!current.containsKey(entry.getKey())) {
                deleted.add(entry.getKey().toString());
                difference.add(new Pair<>(entry.getKey().toString(), null));
            }
        }
        return difference;
    }
}
