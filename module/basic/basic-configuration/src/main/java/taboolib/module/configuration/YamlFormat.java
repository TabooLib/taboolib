package taboolib.module.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author TheElectronWill, 坏黑
 */
public final class YamlFormat implements ConfigFormat<CommentedConfig> {

    public static final YamlFormat INSTANCE = new YamlFormat();

    @Override
    public ConfigWriter createWriter() {
        return new YamlWriter();
    }

    @Override
    public ConfigParser<CommentedConfig> createParser() {
        return new YamlParser(this);
    }

    @Override
    public CommentedConfig createConfig(Supplier<Map<String, Object>> mapCreator) {
        return CommentedConfig.of(mapCreator, this);
    }

    @Override
    public boolean supportsComments() {
        return true;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return type == null
                || type.isEnum()
                || type == Boolean.class
                || type == String.class
                || type == java.util.Date.class
                || type == java.sql.Date.class
                || type == java.sql.Timestamp.class
                || type == byte[].class
                || type == Object[].class
                || Number.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type)
                || Config.class.isAssignableFrom(type);
    }
}