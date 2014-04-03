package ru.dpohvar.varscript.utils;

import javax.script.ScriptEngine;
import java.lang.reflect.Method;


/**
 * Change -usejavacp and -bootclasspath variables of scala script engine
 */
public class ScalaOptimizer {

    public static ScriptEngine modify(ScriptEngine engine) throws Throwable{

        Method methodSettings = engine.getClass().getMethod("settings");
        Object settings = methodSettings.invoke(engine);

        Method methodUseJavaCp = settings.getClass().getMethod("usejavacp");
        Object useJavaCp = methodUseJavaCp.invoke(settings);
        Method methodSetUseJavaCpValue = useJavaCp.getClass().getMethod("tryToSetFromPropertyValue", String.class);
        methodSetUseJavaCpValue.invoke(useJavaCp, "true");

        Method methodBootClassPath = settings.getClass().getMethod("bootclasspath");
        Object bootClassPath = methodBootClassPath.invoke(settings);
        Method methodCootClassPathValue = bootClassPath.getClass().getMethod("tryToSetFromPropertyValue", String.class);
        methodCootClassPathValue.invoke(bootClassPath, "lib/scala-library.jar");

        return engine;
    }
}
