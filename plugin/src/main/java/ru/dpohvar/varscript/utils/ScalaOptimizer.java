package ru.dpohvar.varscript.utils;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by DPOH-VAR on 28.02.14
 */
public class ScalaOptimizer {

    public static ScriptEngine modify(ScriptEngine engine, ClassLoader loader) throws Throwable{

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
