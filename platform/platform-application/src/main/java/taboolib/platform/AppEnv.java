package taboolib.platform;

/**
 * TabooLib
 * taboolib.platform.AppEnv
 *
 * @author 坏黑
 * @since 2024/6/28 02:19
 */
public class AppEnv {

    public AppEnv version(String version) {
        System.setProperty("taboolib.version", version);
        return this;
    }

    public AppEnv noKotlin() {
        return kotlinVersion("null").kotlinCoroutinesVersion("null");
    }

    public AppEnv kotlinVersion(String version) {
        System.setProperty("taboolib.kotlin.stdlib", version);
        return this;
    }

    public AppEnv kotlinCoroutinesVersion(String version) {
        System.setProperty("taboolib.kotlin.coroutines", version);
        return this;
    }

    public AppEnv skipKotlinRelocate(boolean skip) {
        System.setProperty("taboolib.skip-relocate.kotlin", String.valueOf(skip));
        return this;
    }

    public AppEnv skipSelfRelocate(boolean skip) {
        System.setProperty("taboolib.skip-relocate.self", String.valueOf(skip));
        return this;
    }

    public AppEnv dev() {
        System.setProperty("taboolib.dev", "true");
        return this;
    }

    public AppEnv debug() {
        System.setProperty("taboolib.debug", "true");
        return this;
    }

    public AppEnv repoCentral(String value) {
        System.setProperty("taboolib.repo.central", value);
        return this;
    }

    public AppEnv repoSelf(String value) {
        System.setProperty("taboolib.repo.self", value);
        return this;
    }

    public AppEnv scan(String... name) {
        String exists = System.getProperty("taboolib.scan", "");
        if (exists.isEmpty()) {
            System.setProperty("taboolib.scan", String.join(",", name));
        } else {
            System.setProperty("taboolib.scan", exists + "," + String.join(",", name));
        }
        return this;
    }

    public AppEnv main(String name) {
        System.setProperty("taboolib.main", name);
        return this;
    }

    public AppEnv group(String group) {
        System.setProperty("taboolib.group", group);
        return this;
    }

    /**
     * 使用本地仓库
     */
    public AppEnv local() {
        return repoSelf("file:" + System.getProperty("user.home") + "/.m2/repository");
    }
}
