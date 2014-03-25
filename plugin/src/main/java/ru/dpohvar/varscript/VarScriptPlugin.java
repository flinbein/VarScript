package ru.dpohvar.varscript;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dpohvar.varscript.binding.ServerBindings;
import ru.dpohvar.varscript.command.*;
import ru.dpohvar.varscript.utils.ScalaOptimizer;
import sun.misc.JarFilter;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static ru.dpohvar.varscript.utils.ReflectionUtils.*;

public class VarScriptPlugin extends JavaPlugin {

    /**
     * plugin instance
     */
    public static VarScriptPlugin plugin;

    public static final String prefix = ChatColor.translateAlternateColorCodes('&',"&2&l[&aVarScript&2&l]&r ");
    public static final String printPrefix = ChatColor.translateAlternateColorCodes('&',"&6&l[&eVarScript&6&l]&r ");
    public static final String errorPrefix = ChatColor.translateAlternateColorCodes('&',"&4&l[&cVarScript&4&l]&r ");
    private Map<String,ScriptEngine> engineByExtensionMap = new HashMap<>();
    private Map<String,ScriptEngine> engineByNameMap = new HashMap<>();
    private Bindings constantBindings = new SimpleBindings();
    private ServerBindings serverBindings = new ServerBindings();
    private WorkspaceManager workspaceManager;

    /**
     * get global server bindings\n
     * it contains variables:\n
     * server - current server\n
     * workspaceManager - {@link #getWorkspaceManager()}\n
     * pluginManager - {@link org.bukkit.Server#getPluginManager()}\n
     * classLoader - {@link #getLibClassLoader()}
     * @return bindings
     */
    public ServerBindings getServerBindings(){
        return serverBindings;
    }

    /**
     * get global constant bindings\n
     * it can be changed by {@link Workspace#putGlobal(String, Object)}
     * @return bindings
     */
    public Bindings getConstantBindings(){
        return constantBindings;
    }

    /**
     * get instance of {@link ru.dpohvar.varscript.WorkspaceManager}
     * @return workspace manager
     */
    public WorkspaceManager getWorkspaceManager(){
        return workspaceManager;
    }

    /**
     * get script engine by given file extension
     * @param ext file extension
     * @return script engine or null
     */
    public ScriptEngine getScriptEngineByExtension(String ext){
        return engineByExtensionMap.get(ext);
    }

    /**
     * get script engine by name or file extension
     * @param name engine name or file extension
     * @return script engine or null
     */
    public ScriptEngine getScriptEngineByName(String name){
        ScriptEngine engine = engineByNameMap.get(name);
        if (engine != null) return engine;
        return getScriptEngineByExtension(name);
    }

    /**
     * get all registered script file extensions
     * @return list of file extensions
     */
    public Collection<String> getScriptExtensions(){
        return new HashSet<>(engineByExtensionMap.keySet());
    }

    /**
     * get all registered script engines
     * @return set of script engines
     */
    public Set<ScriptEngine> getScriptEngines(){
        return new HashSet<>(engineByExtensionMap.values());
    }

    /**
     * get plugin class loader
     * @return class loader
     */
    public URLClassLoader getLibClassLoader() {
        return (URLClassLoader) getClassLoader();

    }

    /**
     * get debug mode\n
     * in this mode all exceptions will be printed to console
     * see key "debug" in config.yml
     * @return debug mode
     */
    public boolean isDebug(){
        return getConfig().getBoolean("debug");
    }

    /**
     * get debug-to-sender mode\n
     * in this mode exceptions will be fully printed to command sender
     * see key "print-stack-trace-to-sender" in config.yml
     * @return debug-to-sender mode
     */
    public boolean isPrintStackTraceToSender(){
        return getConfig().getBoolean("print-stack-trace-to-sender");
    }

    private void loadLibraries(URLClassLoader pluginLoader) {
        RefClass refURLClassLoader = getRefClass(URLClassLoader.class);
        RefMethod addUrlMethod = refURLClassLoader.getMethod("addURL",URL.class);

        File dir = new File("lib");
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new RuntimeException("can not create 'lib' folder");
        }
        try {
            addUrlMethod.of(pluginLoader).call( dir.toURI().toURL() );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        File[] files = dir.listFiles(new JarFilter());
        if (files != null) for (File f : files) try {
            addUrlMethod.of(pluginLoader).call( f.toURI().toURL() );
            getLogger().info("lib: " + f.getName() + " loaded");
        } catch (IOException e) {
            getLogger().info("lib: can not load " + f.getName());
            e.printStackTrace();
        }
    }

    private void loadScriptEngines(ClassLoader loader){
        ScriptEngineManager engineManager = new ScriptEngineManager(loader);
        for (ScriptEngineFactory factory: engineManager.getEngineFactories()) try {
            getLogger().info(
                    "loading " + factory.getEngineName() + " " + factory.getEngineVersion() +
                    "\nlang: " + factory.getLanguageName() + " " + factory.getLanguageVersion() +
                    "\nname: " + StringUtils.join(factory.getNames(), ",") +
                    "\nextension: " + StringUtils.join(factory.getExtensions(), ",")
            );
            ScriptEngine engine;
            try{
                engine = factory.getScriptEngine();
                if (factory.getLanguageName().equals("Scala")) {
                    engine = ScalaOptimizer.modify(engine, loader);
                }
            } catch (Throwable e) {
                getLogger().warning("can not load "+factory.getEngineName());
                e.printStackTrace();
                continue;
            }
            for (String name: factory.getNames()) {
                engineByNameMap.put(name.replace(' ','_'), engine);
            }
            engineByNameMap.put(factory.getLanguageName().replace(' ', '_'), engine);
            for (String name: factory.getExtensions()) {
                engineByExtensionMap.put(name.replace(' ','_'), engine);
            }
            getLogger().info("engine " + factory.getEngineName() + " loaded");
        } catch (Exception e) {
            getLogger().warning("can not load engine: "+factory);
        }
    }

    @Override
    public void onLoad() {
        plugin = this;
        workspaceManager = new WorkspaceManager(this);
        constantBindings.put("server",getServer());
        constantBindings.put("workspaceManager",workspaceManager);
        constantBindings.put("pluginManager",getServer().getPluginManager());
        constantBindings.put("classLoader",getClassLoader());
        loadLibraries(getLibClassLoader());
        loadScriptEngines(getLibClassLoader());
    }

    @Override
    public void onEnable() {
        getCommand("scriptengine").setExecutor(new EngineCommand(this));
        getCommand("workspace").setExecutor(new WorkspaceCommand(workspaceManager));
        getCommand("script").setExecutor(new RunScriptCommand(workspaceManager));
        getCommand("code").setExecutor(new RunCodeCommand(workspaceManager));

        getCommand("python>").setExecutor(new NamedEngineCommand(workspaceManager,"python"));
        getCommand("javascript>").setExecutor(new NamedEngineCommand(workspaceManager,"ECMAScript"));
        getCommand("groovy>").setExecutor(new NamedEngineCommand(workspaceManager,"Groovy"));

        workspaceManager.loadAllWorkspaces();
    }

    @Override
    public void onDisable() {
        workspaceManager.unloadAllWorkspaces();
    }
}
