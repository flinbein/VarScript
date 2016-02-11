package ru.dpohvar.varscript.workspace;

import groovy.lang.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.boot.BootHelper;
import ru.dpohvar.varscript.boot.VarScriptClassLoader;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.utils.FileTime;

import java.io.File;
import java.util.*;

public class WorkspaceService extends GroovyObjectSupport {

    private static boolean usePlayerUniqueId;
    private static boolean useEntityUniqueId;
    static {
        try {
            OfflinePlayer.class.getMethod("getUniqueId");
            usePlayerUniqueId = true;
        } catch (NoSuchMethodException e) {
            usePlayerUniqueId = false;
        }
        try {
            Entity.class.getMethod("getUniqueId");
            useEntityUniqueId = true;
        } catch (NoSuchMethodException e) {
            useEntityUniqueId = false;
        }
    }

    public static Object getSenderHashKey(CommandSender sender){
        if (sender instanceof OfflinePlayer && usePlayerUniqueId) {
            return ((OfflinePlayer) sender).getUniqueId();
        } else if (sender instanceof Entity && useEntityUniqueId) {
            return ((Entity) sender).getUniqueId();
        } else if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock();
        }
        return sender.getName();
    }

    private VarScript varscript;

    private final File autorunDirectory;
    private final File serviceDirectory;
    private final File scriptsDirectory;
    private final File classesDirectory;
    private final GroovyClassLoader groovyClassLoader;
    private final Map<String,Workspace> workspaceMap = new HashMap<String, Workspace>();
    private final List<CompilationCustomizer> compilationCustomizers = new LinkedList<CompilationCustomizer>();
    private final List<String> classPath = new LinkedList<String>();
    private final List<GroovyObject> dynamicModifiers = new ArrayList<GroovyObject>();
    private final Map<FileTime,Class> compiledFileTimeCache = new WeakHashMap<FileTime, Class>();
    private final Map<String,Class> importTabCompleteClasses = new HashMap<String, Class>();
    private final CompilerConfiguration compilerConfiguration;

    private Binding binding = new Binding();

    private static String getClassShortName(String className){
        return className.substring(className.lastIndexOf('.')+1);
    }

    public WorkspaceService(VarScript varscript){
        VarScriptClassLoader libLoader = VarScript.libLoader;
        this.varscript = varscript;
        FileConfiguration config = varscript.getConfig();
        this.autorunDirectory = new File(config.getString("sources.autorun"));
        this.scriptsDirectory = new File(config.getString("sources.scripts"));
        this.classesDirectory = new File(config.getString("sources.classes"));
        this.serviceDirectory = new File(config.getString("sources.services"));
        ImportCustomizer importCustomizer = new ImportCustomizer();
        compilationCustomizers.add(importCustomizer);
        for (Map<?, ?> anImport : config.getMapList("import")) {
            Object scanPackageValue = anImport.get("scan-package");
            String aScanPackage = scanPackageValue != null ? scanPackageValue.toString() : null;
            if (aScanPackage != null) {
                Object recursiveFlag = anImport.get("recursive");
                Object maskFlag = anImport.get("mask");
                boolean recursive = recursiveFlag != null && recursiveFlag.equals(true);
                String mask = maskFlag instanceof String ? (String) maskFlag : null;
                for (String cName : BootHelper.getClassNamesFromPackage(aScanPackage, recursive)) {
                    if (mask != null && !cName.matches(mask)) continue;
                    try {
                        importCustomizer.addImport( getClassShortName(cName), cName);
                        importTabCompleteClasses.put(getClassShortName(cName), libLoader.loadClass(cName));
                    } catch (ClassNotFoundException ignored) {}
                }
            }
            Object classValue = anImport.get("class");
            String aClass = classValue != null ? classValue.toString() : null;
            if (aClass != null) {
                Object aliasFlag = anImport.get("as");
                String alias = aliasFlag != null ? aliasFlag.toString() : getClassShortName(aClass);
                try {
                    Class clazz = libLoader.loadClass(aClass);
                    importTabCompleteClasses.put(alias, clazz);
                    importCustomizer.addImport(alias, aClass);
                } catch (ClassNotFoundException ignored) {}
            }
            Object packageValue = anImport.get("package");
            String aPackage = packageValue != null ? packageValue.toString() : null;
            if (aPackage != null) importCustomizer.addStarImports(aPackage);
        }
        classPath.add(classesDirectory.toString());

        compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(CallerScript.class.getName());
        compilerConfiguration.getCompilationCustomizers().addAll(compilationCustomizers);
        compilerConfiguration.getClasspath().addAll(classPath);
        String encoding = getSourcesEncoding();
        if (encoding != null) compilerConfiguration.setSourceEncoding(encoding);
        groovyClassLoader = new GroovyClassLoader(VarScript.libLoader, compilerConfiguration);
        VarScript.libLoader.monitorFolder(groovyClassLoader, serviceDirectory);
    }

    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }

    public String getSourcesEncoding(){
        return varscript.getConfig().getString("sources.encoding");
    }

    public GroovyClassLoader getGroovyClassLoader() {
        return groovyClassLoader;
    }

    public List<GroovyObject> getDynamicModifiers() {
        return dynamicModifiers;
    }

    public boolean getWorkspaceAutorunState(String name){
        List<String> list = varscript.getConfig().getStringList("autorun");
        return list != null && list.contains(name);
    }

    public List<String> getWorkspaceAutoruns(){
        List<String> list = varscript.getConfig().getStringList("autorun");
        return new ArrayList<String>(list);
    }

    public void setWorkspaceAutorunState(String name, boolean status){
        FileConfiguration config = varscript.getConfig();
        List<String> list = config.getStringList("autorun");
        if (status) list.add(name);
        else list.remove(name);
        config.set("autorun",list);
        varscript.saveConfig();
    }

    public void startAutorun(){
        List<String> list = varscript.getConfig().getStringList("autorun");
        if (list != null) for (String name : list) {
            getOrCreateWorkspace(name);
        }
    }

    public VarScript getVarScript() {
        return varscript;
    }

    public File getScriptsDirectory() {
        return scriptsDirectory;
    }

    public File getClassesDirectory() {
        return classesDirectory;
    }

    public File getServiceDirectory() {
        return serviceDirectory;
    }

    public Binding getBinding() {
        return binding;
    }

    public File getAutorunDirectory(){
        return autorunDirectory;
    }

    public List<CompilationCustomizer> getCompilationCustomizers() {
        return compilationCustomizers;
    }


    public List<String> getClassPath() {
        return classPath;
    }

    public String getWorkspaceName(CommandSender sender){
        FileConfiguration config = varscript.getConfig();
        String workspaceName = config.getString("workspace." + sender.getName());
        if (workspaceName != null) return workspaceName;
        else return sender.getName();
    }

    public void setWorkspaceName(CommandSender sender, String workspaceName){
        FileConfiguration config = varscript.getConfig();
        config.set("workspace." + sender.getName(), workspaceName);
        varscript.saveConfig();
    }

    public Workspace getWorkspace(String workspaceName) {
        return workspaceMap.get(workspaceName);
    }

    public boolean hasWorkspace(Workspace workspace){
        return workspaceMap.get(workspace.getName()) == workspace;
    }

    public Workspace getOrCreateWorkspace(String workspaceName) {
        Workspace workspace = workspaceMap.get(workspaceName);
        if (workspace != null) return workspace;
        workspace = new Workspace(this, workspaceName);
        workspaceMap.put(workspaceName, workspace);
        Object result = workspace.doAutorun();
        if (result != null) {
            Caller caller = varscript.getCallerService().getConsoleCaller();
            caller.sendMessage(DefaultGroovyMethods.toString(result), workspaceName);
        }
        return workspace;
    }

    public void remove(Workspace workspace) {
        if (!workspace.isRemoved()) throw new IllegalArgumentException("Workspace is not disabled");
        String name = workspace.getName();
        if (workspaceMap.get(name) != workspace) throw new IllegalArgumentException("Workspace is not registered");
        workspaceMap.remove(name);
    }

    public Workspace[] getWorkspaces(){
        Collection<Workspace> values = workspaceMap.values();
        Workspace[] result = new Workspace[values.size()];
        return values.toArray(result);
    }

    public Map<String, Class> getImportTabCompleteClasses() {
        return importTabCompleteClasses;
    }

    @Override
    public Object getProperty(String property) {
        return binding.getVariable(property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        binding.setVariable(property, newValue);
    }

    public void removeProperty(String variable){
        binding.getVariables().remove(variable);
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        Object[] arguments;
        if (args instanceof Object[]) arguments = (Object[]) args;
        else arguments = new Object[]{args};

        try {
            return getMetaClass().invokeMethod(this, name, args);
        } catch (MissingMethodException ignored){}

        try {
            Object variable = binding.getVariable(name);
            if (variable instanceof Closure) return ((Closure) variable).call(arguments);
            else InvokerHelper.invokeMethod(variable, "call", arguments);
        }
        catch (MissingMethodException ignored){}
        catch (MissingPropertyException ignored){}

        throw new MissingMethodException(name, this.getClass(), arguments);
    }

    public Class getCompiledFileTimeCache(FileTime fileTime){
        return compiledFileTimeCache.get(fileTime);
    }

    public Class setCompiledFileTimeCache(FileTime fileTime, Class cache){
        return compiledFileTimeCache.put(fileTime, cache);
    }
}



























