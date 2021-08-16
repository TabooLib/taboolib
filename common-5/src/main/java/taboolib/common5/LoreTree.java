package taboolib.common5;

import taboolib.common.Isolated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 只读Lore对象存储树
 * 用于物品Lore前缀识别功能
 * 查询lore即可高效返回对象
 *
 * @author YiMiner
 **/
@Isolated
public class LoreTree<T> {

    private final TrieNode root = new TrieNode();

    private final boolean ignoreSpace;
    private final boolean ignoreColor;
    private final boolean fullMatch;

    public class TrieNode {
        TrieNode pre = null;
        T obj = null;
        int depth = 0;
        final ConcurrentHashMap<LoreChar, TrieNode> child = new ConcurrentHashMap<>();
    }

    /**
     * 初始化一个前缀树
     *
     * @param fullMatch   是否要完全匹配, 默认只需匹配到前缀
     * @param ignoreSpace 是否无视空格
     * @param ignoreColor 是否无视颜色
     **/
    public LoreTree(boolean fullMatch, boolean ignoreSpace, boolean ignoreColor) {
        this.fullMatch = fullMatch;
        this.ignoreSpace = ignoreSpace;
        this.ignoreColor = ignoreColor;
    }

    /**
     * 初始化一个Lore树, 匹配lore前缀, 无视颜色和空格
     **/
    public LoreTree() {
        this(false, true, true);
    }

    /**
     * 向前缀树中添加lore和对应的对象
     *
     * @param lore   物品lore
     * @param object 要放入的对象
     **/
    public void add(String lore, T object) {
        lore = lore.replaceAll("&", "§");
        if (ignoreSpace) {
            lore = lore.replaceAll("\\s", "");
        }
        if (ignoreColor) {
            lore = lore.replaceAll("§.", "");
        }
        int depth = 0;
        TrieNode current = root;
        while (depth < lore.length()) {
            LoreChar c = new LoreChar(lore.charAt(depth));
            if (current.child.containsKey(c)) {
                current = current.child.get(c);
            } else {
                TrieNode node = new TrieNode();
                node.depth++;
                node.pre = current;
                current.child.put(c, node);
                current = node;
            }
            if (depth == lore.length() - 1) {
                current.obj = object;
            }
            depth++;
        }
    }


    /**
     * 查询lore对应的对象
     *
     * @return 查到的对象. 无则返回null
     **/

    public T get(String lore) {
        int depth = 0;
        if (ignoreSpace) {
            lore = lore.replaceAll("\\s", "");
        }
        if (ignoreColor) {
            lore = lore.replaceAll("§.", "");
        }
        TrieNode current = root;
        while (depth < lore.length()) {
            LoreChar c = new LoreChar(lore.charAt(depth));
            TrieNode node = current.child.get(c);
            if (node == null) {
                return null;
            }
            if (node.obj != null) {
                if (!fullMatch) {
                    return node.obj;
                } else if (depth == lore.length() - 1) {
                    return node.obj;
                }
            }
            current = node;
            depth++;
        }
        return null;
    }

    public void clear() {
        root.child.clear();
    }


    /**
     * 返回树中所有有效的lore
     *
     * @return 树中lore的列表
     **/
    public List<String> getLore() {
        List<String> result = new ArrayList<>();
        traverseStr(result, "", root);
        return result;
    }

    /**
     * 返回树中所有有效的对象
     *
     * @return 树中对象的列表
     **/
    public List<T> getObject() {
        List<T> result = new ArrayList<>();
        traverseObj(result, root);
        return result;
    }

    /**
     * 返回树中所有键值对
     *
     * @return 树中所有的lore-对象的HashMap
     **/
    public HashMap<String, T> getMap() {
        HashMap<String, T> result = new HashMap<>();
        traverseMap(result, "", root);
        return result;
    }


    /**
     * 三个函数分开写而不是全用traverseMap
     * 减少最后那一次遍历
     */
    private void traverseStr(List<String> result, String prefix, TrieNode node) {
        for (Map.Entry<LoreChar, TrieNode> entry : node.child.entrySet()) {
            String current = prefix + entry.getKey().get();
            if (entry.getValue().obj != null) {
                result.add(current);
            }
            traverseStr(result, current, entry.getValue());
        }
    }

    private void traverseObj(List<T> result, TrieNode node) {
        for (Map.Entry<LoreChar, TrieNode> entry : node.child.entrySet()) {
            if (entry.getValue().obj != null) {
                result.add(entry.getValue().obj);
            }
            traverseObj(result, entry.getValue());
        }
    }

    private void traverseMap(HashMap<String, T> result, String prefix, TrieNode node) {
        for (Map.Entry<LoreChar, TrieNode> entry : node.child.entrySet()) {
            String current = prefix + entry.getKey().get();
            if (entry.getValue().obj != null) {
                result.put(current, entry.getValue().obj);
            }
            traverseMap(result, current, entry.getValue());
        }
    }

    public static class LoreChar {

        private final char c;

        public LoreChar(char c) {
            this.c = c;
        }

        public char get() {
            return c;
        }

        @Override
        public int hashCode() {
            return c;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof LoreChar) && ((LoreChar) o).c == c;
        }
    }
}