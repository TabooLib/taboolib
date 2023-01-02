package taboolib.common.env;

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
			Class<EnableIsolatedClassLoader> clazz = EnableIsolatedClassLoader.class;
			isEnabled = true;
			
			excludeClasses.addAll(Arrays.asList(
					"taboolib.common.env.IsolatedClassLoader",
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
		} catch (NoClassDefFoundError ignored) {
		}
	}
	
	
	public IsolatedClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			// First, check if the class has already been loaded
			Class<?> c = findLoadedClass(name);
			
			if (c == null) {
				// check isolated classes
				if (!excludeClasses.contains(name)) {
					try {
						c = findClass(name);
					} catch (ClassNotFoundException ignored) {
					}
				}

				// check the parent class loader
				if (c == null) {
					ClassLoader parent = getParent();
					if (parent != null) {
						c = parent.loadClass(name);
					}
				}
			}

			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
	}
	
	
	public static boolean isEnabled() {
		return isEnabled;
	}
	
}
