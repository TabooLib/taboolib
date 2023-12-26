package taboolib.library.configuration;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

public class YamlRepresenter extends Representer {

    public YamlRepresenter(DumperOptions options) {
        super(options);
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
    }

    class RepresentConfigurationSection extends RepresentMap {

        @Override
        public Node representData(Object data) {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }
    }
}
