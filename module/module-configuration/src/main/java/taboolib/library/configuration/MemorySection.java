package taboolib.library.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A type of {@link ConfigurationSection} that is stored in memory.
 */
public class MemorySection implements ConfigurationSection {

    protected final Map<String, Object> map = new LinkedHashMap<>();

    private final ConfigurationDefault root;
    private final ConfigurationSection parent;
    private final String path;
    private final String fullPath;

    /**
     * Creates an empty MemorySection for use as a root {@link ConfigurationDefault}
     * section.
     * <p>
     * Note that calling this without being yourself a {@link ConfigurationDefault}
     * will throw an exception!
     *
     * @throws IllegalStateException Thrown if this is not a {@link
     *                               ConfigurationDefault} root.
     */
    protected MemorySection() {
        if (!(this instanceof ConfigurationDefault)) {
            throw new IllegalStateException("Cannot construct a root MemorySection when not a Configuration");
        }
        this.path = "";
        this.fullPath = "";
        this.parent = null;
        this.root = (ConfigurationDefault) this;
    }

    /**
     * Creates an empty MemorySection with the specified parent and path.
     *
     * @param parent Parent section that contains this own section.
     * @param path   Path that you may access this section from via the root
     *               {@link ConfigurationDefault}.
     * @throws IllegalArgumentException Thrown is parent or path is null, or
     *                                  if parent contains no root Configuration.
     */
    protected MemorySection(ConfigurationSection parent, String path) {
        this.path = path;
        this.parent = parent;
        this.root = parent.getRoot();
        this.fullPath = createPath(parent, path);
    }

    @NotNull
    public Set<String> getKeys(boolean deep) {
        Set<String> result = new LinkedHashSet<>();
        ConfigurationDefault root = getRoot();
        if (root != null && root.options().copyDefaults()) {
            ConfigurationSection defaults = getDefaultSection();
            if (defaults != null) {
                result.addAll(defaults.getKeys(deep));
            }
        }
        mapChildrenKeys(result, this, deep);
        return result;
    }

    @NotNull
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> result = new LinkedHashMap<>();
        ConfigurationDefault root = getRoot();
        if (root != null && root.options().copyDefaults()) {
            ConfigurationSection defaults = getDefaultSection();
            if (defaults != null) {
                result.putAll(defaults.getValues(deep));
            }
        }
        mapChildrenValues(result, this, deep);
        return result;
    }

    public boolean contains(@NotNull String path) {
        return get(path) != null;
    }

    public boolean isSet(@NotNull String path) {
        ConfigurationDefault root = getRoot();
        if (root == null) {
            return false;
        }
        if (root.options().copyDefaults()) {
            return contains(path);
        }
        return get(path, null) != null;
    }

    @NotNull
    public String getCurrentPath() {
        return fullPath;
    }

    @NotNull
    public String getName() {
        return path;
    }

    @Nullable
    public ConfigurationDefault getRoot() {
        return root;
    }

    @Nullable
    public ConfigurationSection getParent() {
        return parent;
    }

    public void addDefault(@NotNull String path, Object value) {
        ConfigurationDefault root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot add default without root");
        }
        if (root == this) {
            throw new UnsupportedOperationException("Unsupported addDefault(String, Object) implementation");
        }
        root.addDefault(createPath(this, path), value);
    }

    @Nullable
    public ConfigurationSection getDefaultSection() {
        ConfigurationDefault root = getRoot();
        ConfigurationDefault defaults = root == null ? null : root.getDefaults();
        if (defaults != null && defaults.isConfigurationSection(getCurrentPath())) {
            return defaults.getConfigurationSection(getCurrentPath());
        }
        return null;
    }

    public void set(@NotNull String path, Object value) {
        ConfigurationDefault root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot use section without a root");
        }
        final char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }
        String key = path.substring(i2);
        if (section == this) {
            if (value == null) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        } else {
            section.set(key, value);
        }
    }

    public Object get(@NotNull String path) {
        return get(path, getDefault(path));
    }

    public Object get(@NotNull String path, Object def) {
        if (path.length() == 0) {
            return this;
        }
        ConfigurationDefault root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot access section without a root");
        }
        final char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            section = section.getConfigurationSection(path.substring(i2, i1));
            if (section == null) {
                return def;
            }
        }
        String key = path.substring(i2);
        if (section == this) {
            Object result = map.get(key);
            return (result == null) ? def : result;
        }
        return section.get(key, def);
    }

    @NotNull
    public ConfigurationSection createSection(@NotNull String path) {
        ConfigurationDefault root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create section without a root");
        }
        final char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }
        String key = path.substring(i2);
        if (section == this) {
            ConfigurationSection result = new MemorySection(this, key);
            map.put(key, result);
            return result;
        }
        return section.createSection(key);
    }

    @NotNull
    public ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        ConfigurationSection section = createSection(path);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
            } else {
                section.set(entry.getKey().toString(), entry.getValue());
            }
        }
        return section;
    }

    // Primitives
    public String getString(@NotNull String path) {
        Object def = getDefault(path);
        return getString(path, def != null ? def.toString() : null);
    }

    public String getString(@NotNull String path, String def) {
        Object val = get(path, def);
        return (val != null) ? val.toString() : def;
    }

    public boolean isString(@NotNull String path) {
        Object val = get(path);
        return val instanceof String;
    }

    public int getInt(@NotNull String path) {
        Object def = getDefault(path);
        return getInt(path, (def instanceof Number) ? ((Number) def).intValue() : 0);
    }

    public int getInt(@NotNull String path, int def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).intValue() : def;
    }

    public boolean isInt(@NotNull String path) {
        Object val = get(path);
        return val instanceof Integer;
    }

    public boolean getBoolean(@NotNull String path) {
        Object def = getDefault(path);
        return getBoolean(path, (def instanceof Boolean) ? (Boolean) def : false);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        Object val = get(path, def);
        return (val instanceof Boolean) ? (Boolean) val : def;
    }

    public boolean isBoolean(@NotNull String path) {
        Object val = get(path);
        return val instanceof Boolean;
    }

    public double getDouble(@NotNull String path) {
        Object def = getDefault(path);
        return getDouble(path, (def instanceof Number) ? ((Number) def).doubleValue() : 0);
    }

    public double getDouble(@NotNull String path, double def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).doubleValue() : def;
    }

    public boolean isDouble(@NotNull String path) {
        Object val = get(path);
        return val instanceof Double;
    }

    public long getLong(@NotNull String path) {
        Object def = getDefault(path);
        return getLong(path, (def instanceof Number) ? ((Number) def).longValue() : 0);
    }

    public long getLong(@NotNull String path, long def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).longValue() : def;
    }

    public boolean isLong(@NotNull String path) {
        Object val = get(path);
        return val instanceof Long;
    }

    // Java
    public List<?> getList(@NotNull String path) {
        Object def = getDefault(path);
        return getList(path, (def instanceof List) ? (List<?>) def : null);
    }

    public List<?> getList(@NotNull String path, List<?> def) {
        Object val = get(path, def);
        return (List<?>) ((val instanceof List) ? val : def);
    }

    public boolean isList(@NotNull String path) {
        Object val = get(path);
        return val instanceof List;
    }

    @NotNull
    public List<String> getStringList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<String> result = new ArrayList<>();
        for (Object object : list) {
            if ((object instanceof String) || (isPrimitiveWrapper(object))) {
                result.add(String.valueOf(object));
            }
        }
        return result;
    }

    @NotNull
    public List<Integer> getIntegerList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Integer> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Integer) {
                result.add((Integer) object);
            } else if (object instanceof String) {
                try {
                    result.add(Integer.valueOf((String) object));
                } catch (Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((int) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).intValue());
            }
        }
        return result;
    }

    @NotNull
    public List<Boolean> getBooleanList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Boolean> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Boolean) {
                result.add((Boolean) object);
            } else if (object instanceof String) {
                if (Boolean.TRUE.toString().equals(object)) {
                    result.add(true);
                } else if (Boolean.FALSE.toString().equals(object)) {
                    result.add(false);
                }
            }
        }
        return result;
    }

    @NotNull
    public List<Double> getDoubleList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Double> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Double) {
                result.add((Double) object);
            } else if (object instanceof String) {
                try {
                    result.add(Double.valueOf((String) object));
                } catch (Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((double) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).doubleValue());
            }
        }
        return result;
    }

    @NotNull
    public List<Float> getFloatList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Float> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Float) {
                result.add((Float) object);
            } else if (object instanceof String) {
                try {
                    result.add(Float.valueOf((String) object));
                } catch (Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((float) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).floatValue());
            }
        }
        return result;
    }

    @NotNull
    public List<Long> getLongList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Long> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Long) {
                result.add((Long) object);
            } else if (object instanceof String) {
                try {
                    result.add(Long.valueOf((String) object));
                } catch (Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((long) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).longValue());
            }
        }
        return result;
    }

    @NotNull
    public List<Byte> getByteList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Byte> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Byte) {
                result.add((Byte) object);
            } else if (object instanceof String) {
                try {
                    result.add(Byte.valueOf((String) object));
                } catch (Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((byte) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).byteValue());
            }
        }

        return result;
    }

    @NotNull
    public List<Character> getCharacterList(@NotNull String path) {
        List<?> list = getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        List<Character> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Character) {
                result.add((Character) object);
            } else if (object instanceof String) {
                String str = (String) object;

                if (str.length() == 1) {
                    result.add(str.charAt(0));
                }
            } else if (object instanceof Number) {
                result.add((char) ((Number) object).intValue());
            }
        }

        return result;
    }

    @NotNull
    public List<Short> getShortList(@NotNull String path) {
        List<?> list = getList(path);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Short> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Short) {
                result.add((Short) object);
            } else if (object instanceof String) {
                try {
                    result.add(Short.valueOf((String) object));
                } catch (Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((short) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).shortValue());
            }
        }
        return result;
    }

    @NotNull
    public List<Map<?, ?>> getMapList(@NotNull String path) {
        List<?> list = getList(path);
        List<Map<?, ?>> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object object : list) {
            if (object instanceof Map) {
                result.add((Map<?, ?>) object);
            }
        }
        return result;
    }

    public ConfigurationSection getConfigurationSection(@NotNull String path) {
        Object val = get(path, null);
        if (val != null) {
            return (val instanceof ConfigurationSection) ? (ConfigurationSection) val : null;
        }
        val = get(path, getDefault(path));
        return (val instanceof ConfigurationSection) ? createSection(path) : null;
    }

    public boolean isConfigurationSection(@NotNull String path) {
        Object val = get(path);
        return val instanceof ConfigurationSection;
    }

    protected boolean isPrimitiveWrapper(Object input) {
        return input instanceof Integer || input instanceof Boolean ||
                input instanceof Character || input instanceof Byte ||
                input instanceof Short || input instanceof Double ||
                input instanceof Long || input instanceof Float;
    }

    protected Object getDefault(String path) {
        ConfigurationDefault root = getRoot();
        ConfigurationDefault defaults = root == null ? null : root.getDefaults();
        return (defaults == null) ? null : defaults.get(createPath(this, path));
    }

    protected void mapChildrenKeys(Set<String> output, ConfigurationSection section, boolean deep) {
        if (section instanceof MemorySection) {
            MemorySection sec = (MemorySection) section;
            for (Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.add(createPath(section, entry.getKey(), this));
                if (entry.getValue() instanceof ConfigurationSection && deep) {
                    ConfigurationSection subsection = (ConfigurationSection) entry.getValue();
                    mapChildrenKeys(output, subsection, deep);
                }
            }
        } else {
            Set<String> keys = section.getKeys(deep);
            for (String key : keys) {
                output.add(createPath(section, key, this));
            }
        }
    }

    protected void mapChildrenValues(Map<String, Object> output, ConfigurationSection section, boolean deep) {
        if (section instanceof MemorySection) {
            MemorySection sec = (MemorySection) section;
            for (Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.put(createPath(section, entry.getKey(), this), entry.getValue());
                if (entry.getValue() instanceof ConfigurationSection && deep) {
                    mapChildrenValues(output, (ConfigurationSection) entry.getValue(), deep);
                }
            }
        } else {
            Map<String, Object> values = section.getValues(deep);
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                output.put(createPath(section, entry.getKey(), this), entry.getValue());
            }
        }
    }

    /**
     * Creates a full path to the given {@link ConfigurationSection} from its
     * root {@link ConfigurationDefault}.
     * <p>
     * You may use this method for any given {@link ConfigurationSection}, not
     * only {@link MemorySection}.
     *
     * @param section Section to create a path for.
     * @param key     Name of the specified section.
     * @return Full path of the section from its root.
     */
    public static String createPath(ConfigurationSection section, String key) {
        return createPath(section, key, section.getRoot());
    }

    /**
     * Creates a relative path to the given {@link ConfigurationSection} from
     * the given relative section.
     * <p>
     * You may use this method for any given {@link ConfigurationSection}, not
     * only {@link MemorySection}.
     *
     * @param section    Section to create a path for.
     * @param key        Name of the specified section.
     * @param relativeTo Section to create the path relative to.
     * @return Full path of the section from its root.
     */
    public static String createPath(ConfigurationSection section, String key, ConfigurationSection relativeTo) {
        ConfigurationDefault root = section.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create path without a root");
        }
        char separator = root.options().pathSeparator();
        StringBuilder builder = new StringBuilder();
        for (ConfigurationSection parent = section; (parent != null) && (parent != relativeTo); parent = parent.getParent()) {
            if (builder.length() > 0) {
                builder.insert(0, separator);
            }
            builder.insert(0, parent.getName());
        }
        if ((key != null) && (key.length() > 0)) {
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(key);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        ConfigurationDefault root = getRoot();
        return getClass().getSimpleName() +
                "[path='" +
                getCurrentPath() +
                "', root='" +
                (root == null ? null : root.getClass().getSimpleName()) +
                "']";
    }
}
