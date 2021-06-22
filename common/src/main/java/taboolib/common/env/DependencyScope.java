package taboolib.common.env;

/**
 * The scope of a dependency
 * 
 * @author Zach Deibert
 * @since 1.0.0
 */
public enum DependencyScope {

	/**
	 * The dependency is needed when the code is being compiled, so it will be
	 * downloaded while resolving dependencies at runtime.
	 * 
	 * @since 1.0.0
	 */
	COMPILE,

	/**
	 * The dependency is provided by the runtime environment, so it does not
	 * need to be downloaded while resolving dependencies at runtime.
	 * 
	 * @since 1.0.0
	 */
	PROVIDED,

	/**
	 * The dependency is needed when the application is running, so it will be
	 * downloaded while resolving dependencies at runtime.
	 * 
	 * @since 1.0.0
	 */
	RUNTIME,

	/**
	 * The dependency is needed for compiling and running the unit tests, so it
	 * does not need to be downloaded while resolving dependencies at runtime.
	 * 
	 * @since 1.0.0
	 */
	TEST,

	/**
	 * The dependency should be on the system already, so it does not need to be
	 * downloaded while resolving dependencies at runtime.
	 * 
	 * @since 1.0.0
	 */
	SYSTEM,

	/**
	 * The dependency is actually just a pom and not a jar, so we do not need to
	 * download it at all.
	 * 
	 * @since 1.0.0
	 */
	IMPORT
}
