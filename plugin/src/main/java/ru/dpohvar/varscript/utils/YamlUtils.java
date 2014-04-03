package ru.dpohvar.varscript.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.dpohvar.varscript.VarScriptPlugin;

import java.io.*;

public class YamlUtils {
    private static Yaml yaml;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
    }

    public static Object loadYaml(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF8")) {
            return yaml.load(reader);
        } catch (IOException e) {
            if (VarScriptPlugin.plugin.isDebug()) e.printStackTrace();
            return null;
        }
    }

    public static boolean dumpYaml(File file, Object value) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF8")) {
            yaml.dump(value, writer);
            return true;
        } catch (IOException e) {
            if (VarScriptPlugin.plugin.isDebug()) e.printStackTrace();
            return false;
        }
    }
}
