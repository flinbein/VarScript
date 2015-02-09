package ru.dpohvar.varscript.workspace;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import org.bukkit.command.ConsoleCommandSender;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.decorate.AbsoluteSetter;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Workspace {

    private static final VariableSetter absoluteSetter = new AbsoluteSetter();

    private final String name;
    private final WorkspaceService workspaceService;
    private final File autorunFile;
    private final GroovyClassLoader groovyClassLoader;
    private final CompilerConfiguration compilerConfiguration;
    private final VariableContainer workspaceVariables;

    public Workspace(WorkspaceService workspaceService, String name) {
        this.workspaceService = workspaceService;
        this.name = name;
        this.workspaceVariables = new VariableContainer(workspaceService.getGlobalVariables());
        this.workspaceVariables.getSoftDynamicSetters().add(absoluteSetter);
        File autorunDirectory = workspaceService.getAutorunDirectory();
        autorunFile = new File(autorunDirectory, name+".groovy");
        compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(CallerScript.class.getName());
        List<CompilationCustomizer> compilationCustomizers = compilerConfiguration.getCompilationCustomizers();
        compilerConfiguration.getClasspath().addAll(workspaceService.getClassPath());
        compilationCustomizers.addAll(workspaceService.getCompilationCustomizers());
        groovyClassLoader = new GroovyClassLoader(VarScript.pluginClassLoader, compilerConfiguration);
    }

    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }

    public GroovyClassLoader getGroovyClassLoader() {
        return groovyClassLoader;
    }

    public String getName() {
        return name;
    }

    public File getAutorunFile() {
        return autorunFile;
    }

    public VariableContainer getWorkspaceVariables() {
        return workspaceVariables;
    }

    public Object doAutorun(){
        if (!autorunFile.isFile()) return null;
        VarScript plugin = workspaceService.getPlugin();
        ConsoleCommandSender sender = plugin.getServer().getConsoleSender();
        Caller caller = plugin.getCallerService().getCaller(sender);
        try {
            return executeScript(caller, autorunFile, null);
        } catch (Exception e) {
            caller.sendThrowable(e);
            return null;
        }
    }

    public Object executeScript(Caller caller, String script, VariableContainer variables) throws Exception {
        String scriptName = caller.getSender().getName() + "@" + this.name;
        Class scriptClass = groovyClassLoader.parseClass(script, scriptName + ".groovy");
        if (!Script.class.isAssignableFrom(scriptClass)) return scriptClass;
        else return executeScript(caller, scriptClass, variables);
    }

    public Object executeScript(Caller caller, File file, VariableContainer variables) throws Exception {
        GroovyCodeSource source = new GroovyCodeSource(file);
        Class scriptClass = groovyClassLoader.parseClass(source, false);
        if (!Script.class.isAssignableFrom(scriptClass)) return scriptClass;
        else return executeScript(caller, scriptClass, variables);
    }

    private Object executeScript(Caller caller, Class scriptClass, VariableContainer variables) throws Exception {
        if (variables == null) variables = new VariableContainer(workspaceVariables);
        Map<String, Object> hardVariables = variables.getHardVariables();
        if (!hardVariables.containsKey("caller")) hardVariables.put("caller", caller);
        if (!hardVariables.containsKey("workspace")) hardVariables.put("workspace", this);
        CallerScript scriptObject = (CallerScript) InvokerHelper.createScript(scriptClass, variables);
        scriptObject.setCaller(caller);
        return scriptObject.run();
    }

    @Override
    public String toString() {
        return name;
    }
}

















