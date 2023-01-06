package taboolib.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IsolatedClassLoader extends URLClassLoader {

	private static final Set<String> excludeClasses = new HashSet<>();
	private static boolean isEnabled = false;

	static {
		try {
			Class<SkipIsolatedClassLoader> clazz = SkipIsolatedClassLoader.class;
		} catch (NoClassDefFoundError ignored) {
			isEnabled = true;
			excludeClasses.addAll(Arrays.asList(
					"taboolib.common.IsolatedClassLoader",
					"taboolib.common.platform.Plugin",
					"taboolib.platform.BukkitPlugin",
					"taboolib.platform.BungeePlugin",
					"taboolib.platform.VelocityPlugin",
					"taboolib.platform.CloudNetV3Plugin",
					"taboolib.platform.NukkitPlugin",
					"taboolib.platform.Sponge7Plugin",
					"taboolib.platform.Sponge8Plugin",
					"taboolib.platform.Sponge9Plugin"
			));
		}
	}

	public IsolatedClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			// First, check if the class has already been loaded
			Class<?> loadedClass = findLoadedClass(name);

			if (loadedClass == null) {
				// check isolated classes
				if (!excludeClasses.contains(name)) {
					try {
						loadedClass = findClass(name);
					} catch (ClassNotFoundException ignored) {
					}
				}
				// check the parent class loader
				if (loadedClass == null) {
					ClassLoader parent = getParent();
					if (parent != null) {
						loadedClass = parent.loadClass(name);
					}
				}
			}

			if (resolve) {
				resolveClass(loadedClass);
			}
			return loadedClass;
		}
	}

	public static boolean isEnabled() {
		return isEnabled;
	}
}
