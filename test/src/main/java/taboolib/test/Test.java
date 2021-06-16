package taboolib.test;

import taboolib.common.TabooLibCommon;
import taboolib.common.env.RuntimeEnv;
import taboolib.module.dependency.RuntimeDependencies;
import taboolib.module.dependency.RuntimeDependency;
import taboolib.module.dependency.RuntimeName;
import taboolib.module.dependency.RuntimeTest;

import javax.script.*;
import java.util.Objects;

/**
 * TabooLib
 * taboolib.common5.Test
 *
 * @author sky
 * @since 2021/6/15 4:37 下午
 */
public class Test {

//    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
//    private static final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
//
//    static {
//        ScriptEngineFactory factory = scriptEngineManager.getEngineFactories().stream().filter(factories -> factories.getEngineName().contains("Nashorn")).findFirst().orElse(null);
//        scriptEngine = Objects.requireNonNull(factory).getScriptEngine("-doe", "--global-per-engine");
//    }

//    public static CompiledScript compile(String script) {
//        try {
//            return ((Compilable) scriptEngine).compile(script);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public static void main(String[] args) throws ScriptException {
        TabooLibCommon.init();
        RuntimeEnv.setup("Oracle Nashorn")
                .check("jdk.nashorn.api.scripting.NashornScriptEngineFactory")
                .add("org.openjdk.nashorn", "nashorn-core", "15.2", "1b67a6139e8e4a51ceaa3a0c836c48a3f1e26fca", "sha-1")
                .run();

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngineFactory factory = scriptEngineManager.getEngineFactories().stream().filter(factories -> factories.getEngineName().contains("Nashorn")).findFirst().orElse(null);
        ScriptEngine scriptEngine = factory.getScriptEngine();
        CompiledScript compile = ((Compilable) scriptEngine).compile("1 + 1");
        System.out.println(compile.eval());
    }
}
