package ru.dpohvar.varscript.workspace;

import groovy.lang.*;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.SimpleType;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.yaml.snakeyaml.Yaml;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.event.CompileFileEvent;
import ru.dpohvar.varscript.event.CompileScriptEvent;
import ru.dpohvar.varscript.trigger.*;
import ru.dpohvar.varscript.utils.FileTime;

import java.io.*;
import java.util.*;

public class Workspace extends GroovyObjectSupport implements TriggerGenerator {

    private boolean disabled = false;
    private boolean removed = false;
    private final String name;
    private final WorkspaceService workspaceService;
    private final File autorunFile;
    private final File autorunDirectory;
    private final GroovyClassLoader groovyClassLoader;
    private final CompilerConfiguration compilerConfiguration;
    private final Binding binding = new Binding();
    private final WeakHashMap<String,Class<?>> cacheClasses = new WeakHashMap<>();

    public Workspace(WorkspaceService workspaceService, String name) {
        this.workspaceService = workspaceService;
        this.name = name;
        File autorunDirectory = workspaceService.getAutorunDirectory();
        autorunFile = new File(autorunDirectory, name+".groovy");
        this.autorunDirectory = new File(autorunDirectory, name);
        compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(CallerScript.class.getName());
        List<CompilationCustomizer> compilationCustomizers = compilerConfiguration.getCompilationCustomizers();
        compilerConfiguration.getClasspath().addAll(workspaceService.getClassPath());
        String encoding = workspaceService.getVarScript().getConfig().getString("sources.encoding");
        if (encoding != null) compilerConfiguration.setSourceEncoding(encoding);
        compilationCustomizers.addAll(workspaceService.getCompilationCustomizers());
        groovyClassLoader = new GroovyClassLoader(workspaceService.getGroovyClassLoader(), compilerConfiguration);
    }

    @Override
    public Workspace getWorkspace() {
        return this;
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

    public File getAutorunFileExists() {
        if (!autorunFile.isFile()) return null;
        return checkCanonicalName(autorunFile);
    }

    public static File checkCanonicalName(File file){
        try { // check file name for non-case-sensitive FS
            String canonicalName = file.getCanonicalFile().getName();
            if (!canonicalName.equals(file.getName())) return null;
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    public File getAutorunDirectory() {
        return autorunDirectory;
    }

    public File getAutorunDirectoryExists() {
        if (!autorunDirectory.isDirectory()) return null;
        return checkCanonicalName(autorunDirectory);
    }

    public Binding getBinding() {
        return binding;
    }

    public Object doAutorun(){
        Object result = null;
        Caller caller = workspaceService.getVarScript().getCallerService().getConsoleCaller();
        if (getAutorunDirectoryExists() != null) {
            result = doAutorunService(caller);
        }
        if (getAutorunFileExists() != null) {
            result = doAutorunFile(caller);
        }
        return result;
    }

    public Object doAutorunFile(Caller caller){
        try {
            return executeScript(caller, autorunFile, null);
        } catch (Throwable e) {
            caller.sendThrowable(e, this.getName());
            return null;
        }
    }

    private Object doAutorunService(Caller caller){
        String serviceFileName = readYamlAutorunName();
        if (serviceFileName == null) serviceFileName = "main.groovy";
        File serviceFile = checkCanonicalName(new File(autorunDirectory, serviceFileName));
        if (serviceFile == null) return null;
        try {
            return executeScript(caller, serviceFile, null);
        } catch (Throwable e) {
            caller.sendThrowable(e, this.getName());
            return null;
        }
    }

    private String readYamlAutorunName(){
        File yamlFile = new File(autorunDirectory, "config.yml");
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(yamlFile);
            InputStreamReader reader;
            String charset = workspaceService.getSourcesEncoding();
            if (charset == null) reader = new InputStreamReader(inputStream);
            else reader = new InputStreamReader(inputStream, charset);
            Yaml yaml = new Yaml();
            Object data = yaml.load(reader);
            if (!(data instanceof Map)) return null;
            Object main = ((Map) data).get("main");
            if (main instanceof String) return (String) main;
            else return null;
        } catch (FileNotFoundException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Object compileScript(Caller caller, String script, Binding binding) throws IllegalAccessException, InstantiationException {
        if (caller == null) caller = getWorkspaceService().getVarScript().getCallerService().getConsoleCaller();
        String scriptName = caller.getSender().getName() + "@" + this.name;
        String hashString = scriptName + '\t' + script;
        Class scriptClass = cacheClasses.get(hashString);
        if (scriptClass == null) {
            GroovyCodeSource source = new GroovyCodeSource(script, scriptName, "/groovy/script");
            scriptClass = groovyClassLoader.parseClass(source, false);
            cacheClasses.put(hashString, scriptClass);
        }
        if (!CallerScript.class.isAssignableFrom(scriptClass)) return scriptClass;
        else return ((CallerScript)scriptClass.newInstance()).initializeScript(this, caller, binding);
    }

    public Object compileScript(Caller caller, File file, Binding binding) throws IllegalAccessException, InstantiationException, IOException {

        CompileFileEvent event = new CompileFileEvent(caller, file, this);
        workspaceService.getVarScript().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;
        file = event.getFile();


        if (!file.isFile()) throw new IllegalArgumentException("file not found: "+file);
        FileTime fileTime = new FileTime(file);
        Class scriptClass = workspaceService.getCompiledFileTimeCache(fileTime);
        if (scriptClass == null) {
            GroovyCodeSource source = new GroovyCodeSource(file);
            scriptClass = groovyClassLoader.parseClass(source, false);
            workspaceService.setCompiledFileTimeCache(fileTime, scriptClass);
        }
        if (!CallerScript.class.isAssignableFrom(scriptClass)) return scriptClass;
        else return ((CallerScript)scriptClass.newInstance()).initializeScript(this, caller, binding);
    }

    public Object executeScript(Caller caller, String script, Binding binding) throws Exception {
        Object compileResult = compileScript(caller, script, binding);
        if (compileResult instanceof CallerScript) return ((CallerScript) compileResult).run();
        else return compileResult;
    }

    public Object executeScript(Caller caller, File file, Binding binding) throws Exception {
        Object compileResult = compileScript(caller, file, binding);
        if (compileResult instanceof CallerScript) return ((CallerScript) compileResult).run();
        else return compileResult;
    }

    @Override
    public String toString() {
        return name;
    }

    private Set<Trigger> triggers = new LinkedHashSet<Trigger>();

    @Override
    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass){
        return listen(eventClass, EventPriority.NORMAL, false);
    }

    @Override
    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority){
        return listen(eventClass, priority, false);
    }

    @Override
    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled){
        if (disabled || removed) throw new IllegalStateException("workspace is disabled");
        return new BukkitEventTrigger<T>(this, triggers, eventClass, priority, ignoreCancelled);
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler){
        return listen(eventClass, EventPriority.NORMAL, false, handler);
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler){
        return listen(eventClass, priority, false, handler);
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler){
        Class<?>[] types = handler.getParameterTypes();
        if (types.length > 1) throw new IllegalArgumentException("wrong number of closure params: "+types.length);
        if (types.length == 1 && !types[0].isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException("wrong type of closure param: "+types[0]+", expected: "+eventClass);
        }
        return listen(eventClass, priority, ignoreCancelled).call(handler);
    }


    @Override
    public BukkitEventTrigger listen( Closure closure){
        return listen(EventPriority.NORMAL, closure);
    }

    @Override
    public BukkitEventTrigger listen(EventPriority priority, Closure closure){
        if (disabled || removed) throw new IllegalStateException("workspace is disabled");
        Class<?>[] types = closure.getParameterTypes();
        if (types.length != 1) throw new IllegalArgumentException("wrong number of closure params: "+types.length);
        if (!Event.class.isAssignableFrom(types[0])) {
            throw new IllegalArgumentException(
                    "wrong type of closure param: "+types[0].getName()+", expected: "+Event.class.getName()
            );
        }
        Class<? extends Event> eventClass = types[0].asSubclass(Event.class);
        BukkitEventTrigger trigger = new BukkitEventTrigger<Event>(this, triggers, eventClass, priority, false);
        trigger.setHandler(closure);
        return trigger;
    }

    @Override
    public BukkitTimeoutTrigger timeout(long timeout, boolean sync){
        if (disabled || removed) throw new IllegalStateException("workspace is disabled");
        return new BukkitTimeoutTrigger(this, triggers, timeout, sync);
    }

    @Override
    public BukkitTimeoutTrigger timeout(long timeout){
        return timeout(timeout, true);
    }

    @Override
    public BukkitIntervalTrigger interval(long interval, long timeout, boolean sync){
        if (disabled || removed) throw new IllegalStateException("workspace is disabled");
        return new BukkitIntervalTrigger(this, triggers, timeout, interval, sync);
    }

    @Override
    public BukkitIntervalTrigger interval(long interval, long timeout){
        return interval(interval, timeout, true);
    }

    @Override
    public BukkitIntervalTrigger interval(long interval){
        return interval(interval, interval, true);
    }

    @Override
    public BukkitTimeoutTrigger timeout(long timeout, boolean sync, Closure handler){
        return timeout(timeout, sync).call(handler);
    }

    @Override
    public BukkitTimeoutTrigger timeout(long timeout, Closure handler){
        return timeout(timeout, true).call(handler);
    }

    @Override
    public BukkitIntervalTrigger interval(long interval, long timeout, boolean sync, @ClosureParams(value=SimpleType.class,options={"int","ru.dpohvar.varscript.trigger.BukkitIntervalTrigger"}) Closure handler){
        Class<?>[] types = handler.getParameterTypes();
        if (types.length > 2) throw new IllegalArgumentException("wrong number of closure params: "+types.length);
        if (types.length > 0) {
            if (!types[0].isAssignableFrom(Integer.class) && types[0] != int.class) {
                throw new IllegalArgumentException("wrong type of closure param: "+types[0]+", expected: "+Integer.class);
            }
        }
        if (types.length > 1) {
            if (!types[1].isAssignableFrom(BukkitIntervalTrigger.class)) {
                throw new IllegalArgumentException("wrong type of closure param: "+types[1]+", expected: "+BukkitIntervalTrigger.class);
            }
        }
        return interval(interval, timeout, sync).call(handler);
    }

    @Override
    public BukkitIntervalTrigger interval(long interval, long timeout, @ClosureParams(value=SimpleType.class,options={"int","ru.dpohvar.varscript.trigger.BukkitIntervalTrigger"}) Closure handler){
        return interval(interval, timeout, true, handler);
    }

    @Override
    public BukkitIntervalTrigger interval(long interval, @ClosureParams(value=SimpleType.class,options={"int","ru.dpohvar.varscript.trigger.BukkitIntervalTrigger"}) Closure handler){
        return interval(interval, interval, true, handler);
    }

    @Override
    public StopHookTrigger stopHook(){
        return new StopHookTrigger(this, triggers);
    }

    @Override
    public StopHookTrigger stopHook(Closure closure){
        return stopHook().call(closure);
    }

    @Override
    public CommandTrigger command(String name, String description, String usage, List<String> aliases){
        if (name == null) throw new IllegalArgumentException("command name is required");
        if (disabled || removed) throw new IllegalStateException("workspace is disabled");
        if (description == null) description = "Command generated by VarScript. Workspace: "+name;
        if (usage == null) usage = "";
        if (aliases == null) aliases = new ArrayList<String>();
        return new CommandTrigger(this, triggers, name, description, usage, aliases);
    }

    @Override
    public CommandTrigger command(String name, String description, String usage, List<String> aliases, @ClosureParams(value=SimpleType.class,options={"org.bukkit.command.CommandSender","List<String>","String"})Closure handler){
        Class<?>[] types = handler.getParameterTypes();
        List<Class<?>> inject = Arrays.<Class<?>>asList(CommandSender.class, List.class, String.class);
        List<Integer> argOrder = new ArrayList<Integer>();
        scanClasses: for (Class<?> type : types) {
            for (int i=0; i<inject.size(); i++){
                if (argOrder.contains(i)) continue;
                Class<?> injectClass = inject.get(i);
                if (type.isAssignableFrom(injectClass)) {
                    argOrder.add(i);
                    continue scanClasses;
                }
            }
            throw new IllegalArgumentException("Illegal closure argument of type: "+type.getName());
        }
        return command(name,description,usage, aliases).call(handler);
    }

    @Override
    public CommandTrigger command(Object params) {
        String name = DefaultGroovyMethods.toString(tryGetAttribute(params, "name"));
        String description = DefaultGroovyMethods.toString(tryGetAttribute(params, "description"));
        String usage = DefaultGroovyMethods.toString(tryGetAttribute(params, "usage"));
        List paramAliases = (List) tryGetAttribute(params, "aliases");
        Closure handler = (Closure) tryGetAttribute(params, "handler");
        List<String> aliases = new ArrayList<String>();
        if (paramAliases != null) for (Object alias : paramAliases) aliases.add(DefaultGroovyMethods.toString(alias));
        if (handler == null) return command(name, description, usage, aliases);
        else return command(name, description, usage, aliases, handler);
    }

    private Object tryGetAttribute(Object object, String attribute){
        try {
            return InvokerHelper.getAttribute(object, attribute);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public MetaTriggerGenerator generator() {
        return new TriggerContainer(this, this, triggers);
    }

    @Override
    public TriggerGenerator getParent() {
        return null;
    }

    @Override
    public void stopTriggers() {
        disabled = true;
        for (Trigger trigger : getTriggers()) {
            trigger.stop();
            if (trigger instanceof StopHookTrigger) try {
                ((StopHookTrigger) trigger).run();
            } catch (Exception e) {
                getWorkspaceService().getVarScript().getCallerService().getConsoleCaller().sendThrowable(e, name);
            }
        }
        disabled = removed;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void removeWorkspace(){
        disabled = true;
        removed = true;
        cacheClasses.clear();
        stopTriggers();
        workspaceService.remove(this);
    }

    @Override
    public int triggerCount() {
        return triggers.size();
    }

    @Override
    public Trigger[] getTriggers() {
        Trigger[] result = new Trigger[triggers.size()];
        return triggers.toArray(result);
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

    @SuppressWarnings("Duplicates") @Override
    public Object invokeMethod(String name, Object args) {
        Object[] arguments;
        if (args instanceof Object[]) arguments = (Object[]) args;
        else arguments = new Object[]{args};

        runPickMethod: try {
            Class[] classes = new Class[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                classes[i] = arguments[i].getClass();
            }
            MetaMethod metaMethod = getMetaClass().pickMethod(name, classes);
            if (metaMethod == null) metaMethod = InvokerHelper.getMetaClass(this.getClass()).pickMethod(name, classes);
            if (metaMethod == null) break runPickMethod;
            return metaMethod.doMethodInvoke(this, arguments);
        } catch (MissingMethodException ignored){}

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
}

















