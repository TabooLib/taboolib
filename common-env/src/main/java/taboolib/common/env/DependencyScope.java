package taboolib.common.env;

/**
 * The scope of a dependency
 * 
 * @author Zach Deibert, sky
 * @since 1.0.0
 */
public enum DependencyScope {

	/**
	 * 依赖项在编译代码时需要，因此它将在运行时解析依赖项时下载。
	 */
	COMPILE,

	/**
	 * 依赖项由运行时环境提供，因此在运行时解析依赖项时无需下载。
	 */
	PROVIDED,

	/**
	 * 依赖项在应用程序运行时需要，因此它将在运行时解析依赖项时下载。
	 */
	RUNTIME,

	/**
	 * 依赖项在编译和运行单元测试时需要，因此在运行时解析依赖项时无需下载。
	 */
	TEST,

	/**
	 * 依赖项应该已经在系统上，因此在运行时解析依赖项时无需下载。
	 */
	SYSTEM,

	/**
	 * 依赖项实际上只是一个 pom 而不是一个 jar，因此我们不需要下载它。
	 */
	IMPORT
}
