package io.izzel.taboolib.module.config;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.db.local.SecuredFile;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Pair;
import io.izzel.taboolib.util.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TConfig 配置文件升级工具
 *
 * @author sky
 * @since 2020-04-27 21:02
 */
public class TConfigMigrate {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    /**
     * 更新配置文件
     *
     * @param current 目标文件
     * @param source  源文件
     */
    public static List<String> migrate(InputStream current, InputStream source) {
        boolean migrated = false;
        boolean append = false;
        List<String> content = Files.readToList(current);
        List<String> contentSource = Files.readToList(source);
        String cc = String.join("\n", content);
        String cs = String.join("\n", contentSource);
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
        if (hash1.equals(hash2)) {
            return null;
        }
        List<Pair<String, Update>> update = Lists.newArrayList();
        List<Pair<String, Object>> contrast = contrast(c.getValues(true), s.getValues(true));
        for (Pair<String, Object> pair : contrast) {
            int index = pair.getKey().lastIndexOf(".");
            if (pair.getValue() == null) {
                String[] nodes = pair.getKey().split("\\.");
                Object data = s.get(pair.getKey());
                int find = readNode(pair.getKey(), content);
                if (find == -1) {
                    List<String> commits = readCommit(pair.getKey(), contentSource);
                    update.add(new Pair<>(pair.getKey(), new Update(SecuredFile.dump(data).split("\n"), commits)));
                } else {
                    int line = 1;
                    String[] dump = SecuredFile.dump(data).split("\n");
                    String space = Strings.copy("  ", nodes.length - 1);
                    arrayAppend(content, find + line++, space + "# ------------------------- #");
                    arrayAppend(content, find + line++, space + "#  UPDATE " + dateFormat.format(System.currentTimeMillis()) + "  #");
                    arrayAppend(content, find + line++, space + "# ------------------------- #");
                    for (String commit : readCommit(pair.getKey(), contentSource)) {
                        arrayAppend(content, find + line++, space + "# " + commit);
                    }
                    if (dump.length == 1) {
                        arrayAppend(content, find + line, space + pair.getKey().substring(index + 1) + ": " + dump[0]);
                    } else {
                        Arrays.setAll(dump, i -> space + "  " + dump[i]);
                        arrayAppend(content, find + line, space + pair.getKey().substring(index + 1) + ":");
                        arrayAppend(content, find + line, String.join("\n", dump));
                    }
                    migrated = true;
                }
            }
        }
        if (update.size() > 0) {
            content.add("");
            content.add("# ------------------------- #");
            content.add("#  UPDATE " + dateFormat.format(System.currentTimeMillis()) + "  #");
            content.add("# ------------------------- #");
            for (Pair<String, Update> pair : update) {
                Update value = pair.getValue();
                for (String commit : value.getCommit()) {
                    content.add("# " + commit);
                }
                if (value.getContent().length == 1) {
                    content.add(pair.getKey() + ": " + value.getContent()[0]);
                } else {
                    content.add(pair.getKey() + ":");
                    for (String line : value.getContent()) {
                        content.add("  " + line);
                    }
                }
                content.add("");
            }
            migrated = true;
            append = true;
        }
        if (migrated) {
            if (hash2.isEmpty()) {
                if (!append) {
                    content.add("");
                }
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

    private static <T> void arrayAppend(List<T> list, int index, T element) {
        while (list.size() <= index) {
            list.add((T) "");
        }
        list.add(index, element);
    }

    private static int readNode(String node, List<String> content) {
        String[] nodes = node.split("\\.");
        int find = -1;
        int regex = 0;
        for (int i = 0; i < content.size(); i++) {
            String line = content.get(i).trim();
            for (int j = regex; j < nodes.length; j++) {
                if (line.matches("(['\"])?(" + nodes[j] + ")(['\"])?:(.*)")) {
                    find = i;
                    regex = j;
                    break;
                }
            }
        }
        return find;
    }

    private static List<String> readCommit(String node, List<String> source) {
        List<String> commit = Lists.newArrayList();
        String[] nodes = node.split("\\.");
        int regex = 0;
        for (String i : source) {
            String line = i.trim();
            if (line.startsWith("#")) {
                commit.add(line.substring(1).trim());
            }
            for (int j = regex; j < nodes.length; j++) {
                if (line.matches("(['\"])?(" + nodes[j] + ")(['\"])?:(.*)")) {
                    if (regex == nodes.length - 1) {
                        return commit;
                    }
                    regex = j;
                    break;
                }
            }
            if (!line.startsWith("#") && line.length() != 0) {
                commit.clear();
            }
        }
        return commit;
    }

    static class Node {

        private final int line;
        private final List<String> commit;

        public Node(int line, List<String> commit) {
            this.line = line;
            this.commit = commit;
        }

        public int getLine() {
            return line;
        }

        public List<String> getCommit() {
            return commit;
        }
    }

    static class Update {

        private final String[] value;
        private final List<String> commit;

        public Update(String[] value, List<String> commit) {
            this.value = value;
            this.commit = commit;
        }

        public String[] getContent() {
            return value;
        }

        public List<String> getCommit() {
            return commit;
        }
    }
}
