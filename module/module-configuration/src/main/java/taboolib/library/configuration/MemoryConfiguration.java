package taboolib.library.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This is a {@link ConfigurationDefault} implementation that does not save or load
 * from any source, and stores all values in memory only.
 * This is useful for temporary Configurations for providing defaults.
 */
public class MemoryConfiguration extends MemorySection implements ConfigurationDefault {

    protected ConfigurationDefault defaults;
    protected MemoryConfigurationOptions options;

    /**
     * Creates an empty {@link MemoryConfiguration} with no default values.
     */
    public MemoryConfiguration() {
    }

    /**
     * Creates an empty {@link MemoryConfiguration} using the specified {@link
     * ConfigurationDefault} as a source for all default values.
     *
     * @param defaults Default value provider
     * @throws IllegalArgumentException Thrown if defaults is null
     */
    public MemoryConfiguration(ConfigurationDefault defaults) {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(@NotNull String path, Object value) {
        if (defaults == null) {
            defaults = new MemoryConfiguration();
        }
        defaults.set(path, value);
    }

    public void addDefaults(Map<String, Object> defaults) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    public void addDefaults(ConfigurationDefault defaults) {
        addDefaults(defaults.getValues(true));
    }

    public void setDefaults(ConfigurationDefault defaults) {
        this.defaults = defaults;
    }

    public ConfigurationDefault getDefaults() {
        return defaults;
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    public MemoryConfigurationOptions options() {
        if (options == null) {
            options = new MemoryConfigurationOptions(this);
        }
        return options;
    }
}
