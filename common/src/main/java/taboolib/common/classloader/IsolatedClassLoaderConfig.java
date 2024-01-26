package taboolib.common.classloader;

import java.util.Collections;
import java.util.Set;

public interface IsolatedClassLoaderConfig {
	
	default Set<String> excludedClasses() {
		return Collections.emptySet();
	}
	
	default Set<String> excludedPackages() {
		return Collections.emptySet();
	}
}
