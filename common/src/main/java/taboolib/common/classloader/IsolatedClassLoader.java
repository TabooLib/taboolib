package taboolib.common.classloader;

import taboolib.common.SkipIsolatedClassLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class IsolatedClassLoader extends URLClassLoader {

	private static boolean isEnabled = false;

	public static boolean isEnabled() {
		return isEnabled;
	}

	static {
		try {
			Class<SkipIsolatedClassLoader> clazz = SkipIsolatedClassLoader.class;
		} catch (NoClassDefFoundError ignored) {
			isEnabled = true;
		}
	}

	private final Set<String> excludedClasses = new HashSet<>();
	private final Set<String> excludedPackages = new HashSet<>();
	
	private final MethodHandles.Lookup lookup = MethodHandles.lookup();
	private MethodHandle methodHandleTaboolibCommonRun;

	public IsolatedClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);

		excludedClasses.add("taboolib.common.classloader.IsolatedClassLoader");
		excludedClasses.add("taboolib.common.platform.Plugin");
		excludedPackages.add("java.");

		// Load excluded classes and packages by SPI
		ServiceLoader<IsolatedClassLoaderConfig> serviceLoader = ServiceLoader.load(IsolatedClassLoaderConfig.class, parent);
		for (IsolatedClassLoaderConfig config : serviceLoader) {
			Set<String> configExcludedClasses = config.excludedClasses();
			if (configExcludedClasses != null && !configExcludedClasses.isEmpty())
				excludedClasses.addAll(configExcludedClasses);

			Set<String> configExcludedPackages = config.excludedPackages();
			if (configExcludedPackages != null && !configExcludedPackages.isEmpty())
				excludedPackages.addAll(configExcludedPackages);
		}
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve, true);
	}

	public Class<?> loadClass(String name, boolean resolve, boolean checkParents) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			Class<?> c = findLoadedClass(name);

			// Check isolated classes and libraries before parent to:
			//   - prevent accessing classes of other plugins
			//   - prevent the usage of old patch classes (which stay in memory after reloading)
			if (c == null && !excludedClasses.contains(name)) {
				boolean flag = true;
				for (String excludedPackage : excludedPackages) {
					if (name.startsWith(excludedPackage)) {
						flag = false;
						break;
					}
				}
				if (flag) c = findClassOrNull(name);
			}

			if (c == null && checkParents) {
				c = loadClassFromParentOrNull(name);
			}

			if (c == null) {
				throw new ClassNotFoundException(name);
			}

			if (resolve) {
				resolveClass(c);
			}

			return c;
		}
	}

	private Class<?> findClassOrNull(String name) {
		try {
			return findClass(name);
		} catch (ClassNotFoundException ignored) {
			return null;
		}
	}

	private Class<?> loadClassFromParentOrNull(String name) {
		try {
			return getParent().loadClass(name);
		} catch (ClassNotFoundException ignored) {
			return null;
		}
	}

	public void addExcludedClass(String name) {
		excludedClasses.add(name);
	}

	public void addExcludedClasses(Collection<String> names) {
		excludedClasses.addAll(names);
	}

	public void addExcludedPackage(String name) {
		excludedPackages.add(name);
	}

	public void addExcludedPackages(Collection<String> names) {
		excludedPackages.addAll(names);
	}

	public void runIsolated(Runnable target) throws Throwable {
		getMethodHandleTaboolibCommonRun().invoke(target);
	}
	
	private MethodHandle getMethodHandleTaboolibCommonRun() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
		if (methodHandleTaboolibCommonRun == null) {
			methodHandleTaboolibCommonRun = lookup.findStatic(
				loadClass("taboolib.common.TabooLibCommon"),
				"run",
				MethodType.methodType(void.class, Runnable.class)
			);
		}
		return methodHandleTaboolibCommonRun;
	}
	
}
