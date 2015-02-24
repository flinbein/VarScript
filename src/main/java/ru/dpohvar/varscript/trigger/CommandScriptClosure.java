package ru.dpohvar.varscript.trigger;

import groovy.lang.*;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.workspace.Workspace;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CommandScriptClosure extends Closure {

    private final Workspace workspace;
    private File script;

    public CommandScriptClosure(Workspace workspace) {
        super(workspace);
        this.workspace = workspace;
        this.maximumNumberOfParameters = 3;
    }

    public File getScript() {
        return script;
    }

    public void setScript(File script) {
        this.script = script;
    }

    @Override
    public Object call(Object... arguments) {
        CommandSender sender = (CommandSender) arguments[0];
        List args = (List) arguments[1];
        String command = (String) arguments[2];
        if (!script.isFile()) throw new IllegalStateException("no file: "+script);
        try{
            GroovyCodeSource source = new GroovyCodeSource(script);
            Class scriptClass = workspace.getGroovyClassLoader().parseClass(source, false);
            if (!Script.class.isAssignableFrom(scriptClass)) {
                throw new IllegalStateException("not script: "+script);
            }
            Binding binding = new Binding();
            binding.setVariable("sender", sender);
            binding.setVariable("args", Arrays.asList(args));
            binding.setVariable("command", command);
            return workspace.executeScript(null, script, binding);
        } catch (Throwable throwable){
            throw new GroovyRuntimeException(throwable.getMessage(), throwable);
        }
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[]{CommandSender.class, List.class, String.class};
    }

}