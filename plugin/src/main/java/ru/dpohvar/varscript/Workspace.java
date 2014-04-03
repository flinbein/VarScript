package ru.dpohvar.varscript;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.dpohvar.varscript.binding.DelegateBindings;
import ru.dpohvar.varscript.event.EventHandler;
import ru.dpohvar.varscript.event.EventRegisterResult;
import ru.dpohvar.varscript.exception.PrintTextException;
import ru.dpohvar.varscript.exception.WorkspaceStateException;
import ru.dpohvar.varscript.runner.DelayRunner;
import ru.dpohvar.varscript.runner.PeriodicRunner;
import ru.dpohvar.varscript.runner.Runner;
import ru.dpohvar.varscript.utils.EventUtils;
import ru.dpohvar.varscript.utils.YamlUtils;
import ru.dpohvar.varscript.writer.CommandSenderWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static ru.dpohvar.varscript.VarScriptPlugin.plugin;
import static ru.dpohvar.varscript.utils.ReflectionUtils.*;

/**
 * Workspace <br/>
 * This class represents a session to work with scripts
 */
@SuppressWarnings("UnusedDeclaration")
public class Workspace {

    static final int LOADING = 1;
    static final int LOADED = 2;
    static final int UNLOADED = 4;
    static final int ERROR = 8;
    static final int CREATED = 16;

    static File homeDirectory = new File(VarScriptPlugin.plugin.getDataFolder(), "workspace");

    static {
        if (!homeDirectory.isDirectory() && !homeDirectory.mkdirs()) {
            throw new RuntimeException("can not create dir " + homeDirectory);
        }
    }

    private final File directory;
    private final WorkspaceManager manager;
    private final String name;
    private final File configFile;
    private final DelegateBindings bindings = new DelegateBindings(plugin.getServerBindings());
    private int status = CREATED;
    private Map config = null;
    private Map<String, Workspace> loadSession;

    Workspace(WorkspaceManager manager, String name, Map<String, Workspace> loadSession) {
        this.manager = manager;
        this.name = name;
        this.loadSession = loadSession;
        directory = new File(homeDirectory, name);
        configFile = new File(directory, "workspace.yml");
        if (configFile.isFile()) {
            Object yaml = YamlUtils.loadYaml(configFile);
            if (yaml instanceof Map) config = (Map) yaml;
        }
        if (config == null) {
            config = new HashMap<>();
        }
    }

    /**
     * get home directory of all workspaces<br/>
     * plugins/VarScript/workspace
     *
     * @return home directory
     */
    public static File getHomeDirectory() {
        return homeDirectory;
    }

    /**
     * Get directory of this workspace<br/>
     * For example: plugins/VarScript/workspace/{name}
     *
     * @return directory
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * get workspace config from file workspace.yml
     *
     * @return config
     */
    Map getConfigMap() {
        return config;
    }

    /**
     * save workspace config to file workspace.yml
     *
     * @return true on success
     */
    boolean saveConfigMap() {
        if (config.isEmpty() && !configFile.isFile()) return true;
        return YamlUtils.dumpYaml(configFile, config);
    }

    /**
     * get name of this workspace
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * get status id of this workspace
     *
     * @return status id
     */
    int getStatus() {
        return status;
    }

    boolean load() {
        status = LOADING;
        Object disabled = config.get("disabled");
        if (disabled != null && disabled.equals(true)) {
            status = UNLOADED;
            return false;
        } else try {
            if (directory.isDirectory()) {
                for (String ex : VarScriptPlugin.plugin.getScriptExtensions()) {
                    File main = new File(directory, "Main." + ex);
                    if (main.isFile()) runScript(null, main);
                }
            }
            status = LOADED;
            return true;
        } catch (Throwable e) {
            if (plugin.isDebug()) e.printStackTrace();
            status = UNLOADED;
            stop();
            status = ERROR;
            return false;
        } finally {
            loadSession = null;
        }
    }

    void unload() {
        if (status == UNLOADED) return;
        status = UNLOADED;
        stop();
        manager.unloadWorkspace(this.name);
        bindings.clear();
    }

    /**
     * stop all registered events, timers, etc
     */
    public void stop() {
        for (BukkitTask task : bukkitTasks.values()) task.cancel();
        for (Runner runner : runners.values()) runner.stopRunner();
        for (Map.Entry<String, PluginCommand> e : pluginCommands.entrySet()) {
            e.getValue().unregister(commandMap);
            getKnownCommands().remove(e.getKey());
        }
        for (EventRegisterResult result : listeners.values()) {
            result.handlerList.unregister(result.registeredListener);
        }
        // clone map values to avoid ConcurrentModificationException
        for (Runnable finisher : new ArrayList<>(finishers.values()))
            try {
                finisher.run();
            } catch (Exception e) {
                if (plugin.isDebug()) e.printStackTrace();
            }
        bukkitTasks.clear();
        listeners.clear();
        pluginCommands.clear();
        runners.clear();
        finishers.clear();
    }

    /**
     * Run script file with this workspace
     *
     * @param me   value of variable "me" in script
     * @param file executable file
     * @return result of script
     * @throws IOException              if file is not exist
     * @throws IllegalArgumentException if no script engine
     * @throws ScriptException          error in script
     */
    public Object runScript(Object me, File file) throws ScriptException, IOException {
        String name = file.getName();
        if (!name.contains(".")) throw new PrintTextException(file.toString() + " is not script file");
        String ex = name.substring(name.lastIndexOf(".") + 1, name.length());
        ScriptEngine engine = plugin.getScriptEngineByExtension(ex);
        if (engine == null) throw new PrintTextException("no script engine for file " + file.toString());
        prepareEngine(engine, me);
        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            return engine.eval(reader, buildScriptContext(me));
        }
    }

    /**
     * Run code with this workspace
     *
     * @param me     value of variable "me" in script
     * @param script source code
     * @param lang   scripting language
     * @return result
     * @throws IllegalArgumentException if no script engine or script is null
     * @throws ScriptException          error in script
     */
    public Object runScript(Object me, String script, String lang) throws ScriptException {
        if (script == null) throw new IllegalArgumentException("script is null");
        ScriptEngine engine = plugin.getScriptEngineByName(lang);
        if (engine == null) throw new IllegalArgumentException("no script engine: " + lang);
        prepareEngine(engine, me);
        return engine.eval(script, buildScriptContext(me));
    }

    private ScriptContext buildScriptContext(Object me) {
        DelegateBindings globalBindings = new DelegateBindings(plugin.getConstantBindings());
        globalBindings.put("me", me);
        globalBindings.put("workspace", this);
        ScriptContext context = new SimpleScriptContext();
        context.setBindings(this.bindings, ScriptContext.ENGINE_SCOPE);
        context.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);
        if (me instanceof CommandSender) {
            context.setWriter(new CommandSenderWriter((CommandSender) me, VarScriptPlugin.printPrefix));
            context.setErrorWriter(new CommandSenderWriter((CommandSender) me, VarScriptPlugin.errorPrefix));
        }
        return context;
    }

    private void prepareEngine(ScriptEngine engine, Object me) {
        engine.put("me", me);
        engine.put("workspace", this);
    }

    /**
     * include file by name<br/>
     *
     * @param me       pass variable "me"
     * @param filename file name in workspace directory
     * @return result
     * @throws IOException     if can't read file
     * @throws ScriptException error in script
     * @see #runScript(Object, java.io.File)
     */
    public Object include(Object me, String filename) throws ScriptException, IOException {
        File file = new File(directory, filename);
        return runScript(me, file);
    }

    /**
     * include file by name, don't pass variable "me"
     *
     * @param filename file name
     * @return result
     * @throws IOException     if can't read file
     * @throws ScriptException error in script
     * @see #include(Object, String)
     */
    public Object include(String filename) throws ScriptException, IOException {
        return include(null, filename);
    }

    /**
     * Require to load other workspace before.<br/>
     * Can not be used after loading. Use {@link ru.dpohvar.varscript.WorkspaceManager#getWorkspace(String)}
     *
     * @param workspaceName name of required workspace
     * @return required workspace or null
     * @throws RuntimeException if workspace is fully loaded
     */
    public Workspace require(String workspaceName) throws RuntimeException {
        if (status != LOADING) {
            throw new RuntimeException("can not require workspace after loading");
        }
        return manager.loadWorkspace(workspaceName, loadSession);
    }

    /**
     * Put variable to this bindings
     *
     * @param name name of variable
     * @param val  value of variable
     * @return previous value associated with name
     */
    public Object put(String name, Object val) {
        return this.bindings.put(name, val);
    }

    /**
     * get variable in this bindings
     *
     * @param name name of variable
     * @return value
     */
    public Object get(String name) {
        return this.bindings.get(name);
    }

    /**
     * remove variable from this bindings
     *
     * @param name name of variable
     * @return previous value associated with name
     */
    public Object remove(String name) {
        return this.bindings.remove(name);
    }

    /**
     * put variable to global bindings
     *
     * @param name name of variable
     * @param val  value of variable
     * @return previous value associated with name
     */
    public Object putGlobal(String name, Object val) {
        return plugin.getServerBindings().put(name, val);
    }

    /**
     * get variable in global bindings
     *
     * @param name name of variable
     * @return value
     */
    public Object getGlobal(String name) {
        return plugin.getServerBindings().get(name);
    }

    /**
     * remove variable from global bindings
     *
     * @param name name of variable
     * @return previous value associated with name
     */
    public Object removeGlobal(String name) {
        return plugin.getServerBindings().remove(name);
    }

    /**
     * throw exception and unload workspace
     *
     * @param message message
     */
    public void error(String message) {
        if (status == LOADED) {
            unload();
            status = ERROR;
        } else {
            status = UNLOADED;
            stop();
            status = ERROR;
        }
        throw new RuntimeException(message);
    }

    private static BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
    private HashMap<Long, BukkitTask> bukkitTasks = new HashMap<>();
    long nextBukkitTask = 0;

    /**
     * register periodic bukkit task
     *
     * @param task        the task to be run
     * @param periodTicks the ticks to wait between runs
     * @param delayTicks  the ticks to wait before running the task
     * @param async       enable asynchronous mode
     * @return id of bukkit task
     */
    public synchronized long addPeriod(Runnable task, long periodTicks, long delayTicks, boolean async) {
        if (status != LOADING && status != LOADED) throw new WorkspaceStateException();
        BukkitTask bukkitTask;
        if (async) bukkitTask = bukkitScheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        else bukkitTask = bukkitScheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);
        bukkitTasks.put(nextBukkitTask, bukkitTask);
        return nextBukkitTask++;
    }

    /**
     * register periodic bukkit synchronous task
     *
     * @param task        the task to be run
     * @param periodTicks the ticks to wait between runs
     * @param delayTicks  the ticks to wait before running the task
     * @return id of bukkit task
     * @see #addPeriod(Runnable, long, long, boolean)
     */
    public long addPeriod(Runnable task, long periodTicks, long delayTicks) {
        return addPeriod(task, periodTicks, delayTicks, false);
    }

    /**
     * register periodic bukkit task
     *
     * @param task        the task to be run
     * @param periodTicks the ticks to wait to first run and between runs
     * @param async       enable asynchronous mode
     * @return id of bukkit task
     * @see #addPeriod(Runnable, long, long, boolean)
     */
    public long addPeriod(Runnable task, long periodTicks, boolean async) {
        return addPeriod(task, periodTicks, periodTicks, async);
    }

    /**
     * register periodic bukkit synchronous task
     *
     * @param task        the task to be run
     * @param periodTicks the ticks to wait to first run and between runs
     * @return id of bukkit task
     * @see #addPeriod(Runnable, long, long, boolean)
     */
    public long addPeriod(Runnable task, long periodTicks) {
        return addPeriod(task, periodTicks, periodTicks, false);
    }

    /**
     * stop task, registered with method {@link #addPeriod(Runnable, long, long, boolean)}
     *
     * @param id id of task
     * @return true, if period is stopped
     */
    public synchronized boolean stopPeriod(long id) {
        BukkitTask task = bukkitTasks.remove(id);
        if (task == null) return false;
        task.cancel();
        return true;
    }

    /**
     * register new delayed bukkit task
     *
     * @param task       task to be run
     * @param delayTicks the ticks to wait before running the task
     * @param async      enable asynchronous mode
     * @return id of task
     */
    public synchronized long addDelay(final Runnable task, long delayTicks, boolean async) {
        if (status != LOADING && status != LOADED) throw new WorkspaceStateException();
        BukkitTask bukkitTask;
        final long thisBukkitTask = nextBukkitTask;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                stopDelay(thisBukkitTask);
                task.run();
            }
        };
        if (async) bukkitTask = bukkitScheduler.runTaskLaterAsynchronously(plugin, runnable, delayTicks);
        else bukkitTask = bukkitScheduler.runTaskLater(plugin, runnable, delayTicks);
        bukkitTasks.put(nextBukkitTask, bukkitTask);
        return nextBukkitTask++;
    }

    /**
     * register new delayed bukkit task (in synchronous mode)
     *
     * @param task       task to be run
     * @param delayTicks the ticks to wait before running the task
     * @return id of task
     * @see #addDelay(Runnable, long, boolean)
     */
    public long addDelay(Runnable task, long delayTicks) {
        return addDelay(task, delayTicks, false);
    }

    /**
     * stop delayed bukkit task registered with {@link #addDelay(Runnable, long, boolean)}
     *
     * @param id id of task
     * @return true if task is stopped
     */
    public synchronized boolean stopDelay(long id) {
        BukkitTask task = bukkitTasks.remove(id);
        if (task == null) return false;
        task.cancel();
        return true;
    }

    /**
     * run task in the next server tick
     *
     * @param task task to be run
     */
    public synchronized void runTask(Runnable task) {
        if (status != LOADING && status != LOADED) throw new WorkspaceStateException();
        BukkitTask bukkitTask = bukkitScheduler.runTask(plugin, task);
    }

    private HashMap<Long, Runner> runners = new HashMap<>();
    long nextRunner = 0;

    /**
     * register new asynchronous periodic task<br/>
     * it far more precise than {@link #addPeriod(Runnable, long, long, boolean)}<br/>
     * but you can not use Bukkit API in asynchronous mode
     *
     * @param task         task to be run
     * @param periodMillis milliseconds to wait between runs
     * @param delayMillis  milliseconds to wait before running the task
     * @return asynchronous task id
     */
    public synchronized long addAsyncPeriod(Runnable task, long periodMillis, long delayMillis) {
        if (status != LOADING && status != LOADED) throw new WorkspaceStateException();
        PeriodicRunner runner = new PeriodicRunner(task, periodMillis, delayMillis);
        runners.put(nextRunner, runner);
        runner.start();
        return nextRunner++;
    }

    /**
     * register new asynchronous periodic task<br/>
     * it far more precise than {@link #addPeriod(Runnable, long, long, boolean)}<br/>
     * but you can not use Bukkit API in asynchronous mode
     *
     * @param task         task to be run
     * @param periodMillis milliseconds to wait to first run and between runs
     * @return asynchronous task id
     * @see #addAsyncPeriod(Runnable, long, long)
     */
    public long addAsyncPeriod(Runnable task, long periodMillis) {
        return addAsyncPeriod(task, periodMillis, periodMillis);
    }

    /**
     * stop asynchronous periodic task, registered with {@link #addAsyncPeriod(Runnable, long, long)}
     *
     * @param id id of task
     * @return true, if task is stopped
     */
    public synchronized boolean stopAsyncPeriod(long id) {
        Runner runner = runners.remove(id);
        if (runner == null) return false;
        runner.stopRunner();
        return true;
    }

    /**
     * register new asynchronous delayed task<br/>
     * it far more precise than {@link #addDelay(Runnable, long, boolean)}<br/>
     * but you can not use Bukkit API in asynchronous mode
     *
     * @param task        task to be run
     * @param delayMillis milliseconds to wait before running the task
     * @return id of task
     */
    public synchronized long addAsyncDelay(final Runnable task, long delayMillis) {
        if (status != LOADING && status != LOADED) throw new WorkspaceStateException();
        final long thisRunner = nextRunner;
        final DelayRunner runner = new DelayRunner(new Runnable() {
            @Override
            public void run() {
                stopAsyncDelay(thisRunner);
                task.run();
            }
        }, delayMillis);
        runners.put(nextRunner, runner);
        runner.start();
        return nextRunner++;
    }

    /**
     * stop asynchronous delayed task registered with {@link #addAsyncDelay(Runnable, long)}
     *
     * @param id id of task
     * @return true of task is cancelled
     */
    public synchronized boolean stopAsyncDelay(long id) {
        Runner runner = runners.remove(id);
        if (runner == null) return false;
        runner.stopRunner();
        return true;
    }

    private HashMap<Long, EventRegisterResult> listeners = new HashMap<>();
    long nextListener = 0;

    /**
     * register new event listener
     *
     * @param handler         event handler
     * @param clazz           event class to listen
     * @param priority        priority of listening
     * @param ignoreCancelled set true, if you want to ignore cancelled events
     * @param <T>             bukkit event class
     * @return id of event listener
     */
    public synchronized <T extends Event> long addEvent(EventHandler<T> handler, Class<T> clazz, EventPriority priority, boolean ignoreCancelled) {
        if (status != LOADING && status != LOADED) throw new WorkspaceStateException();
        Class<? extends Event> registrationClass = EventUtils.getRegistrationClass(clazz);
        HandlerList handlerList = EventUtils.getHandlerList(registrationClass);
        RegisteredListener registeredListener;
        registeredListener = EventUtils.createRegisteredListener(plugin, clazz, priority, ignoreCancelled, handler);
        EventRegisterResult result;
        result = new EventRegisterResult(handlerList, clazz, priority, ignoreCancelled, handler, registeredListener);
        listeners.put(nextListener, result);
        handlerList.register(registeredListener);
        return nextListener++;
    }

    /**
     * register new event listener,<br/>
     * not ignoring cancelled events
     *
     * @param handler  event handler
     * @param clazz    event class to listen
     * @param priority priority of listening
     * @param <T>      bukkit event class
     * @return id of event listener
     * @see #addEvent(ru.dpohvar.varscript.event.EventHandler, Class, org.bukkit.event.EventPriority, boolean)
     */
    public <T extends Event> long addEvent(EventHandler<T> handler, Class<T> clazz, EventPriority priority) {
        return addEvent(handler, clazz, priority, false);
    }

    /**
     * register new event listener with NORMAL priority
     *
     * @param handler         event handler
     * @param clazz           event class to listen
     * @param ignoreCancelled set true, if you want to ignore cancelled events
     * @param <T>             bukkit event class
     * @return id of event listener
     */
    public <T extends Event> long addEvent(EventHandler<T> handler, Class<T> clazz, boolean ignoreCancelled) {
        return addEvent(handler, clazz, EventPriority.NORMAL, ignoreCancelled);
    }

    /**
     * register new event listener with NORMAL priority,
     * not ignoring cancelled events
     *
     * @param handler event handler
     * @param clazz   event class to listen
     * @param <T>     bukkit event class
     * @return id of event listener
     */
    public <T extends Event> long addEvent(EventHandler<T> handler, Class<T> clazz) {
        return addEvent(handler, clazz, EventPriority.NORMAL, false);
    }

    /**
     * stop event listener registered with {@link #addEvent(ru.dpohvar.varscript.event.EventHandler, Class, org.bukkit.event.EventPriority, boolean)}
     *
     * @param id id of listener
     * @return true if listener is stopped
     */
    public synchronized boolean stopEvent(long id) {
        EventRegisterResult result = listeners.remove(id);
        if (result == null) return false;
        result.handlerList.unregister(result.registeredListener);
        return true;
    }


    static RefClass<PluginCommand> classPluginCommand = getRefClass(PluginCommand.class);
    static RefConstructor<PluginCommand> pluginCommandConstructor = classPluginCommand.getConstructor(String.class, Plugin.class);
    static RefClass<?> classCraftServer = getRefClass("{cb}.CraftServer");
    static RefMethod<SimpleCommandMap> getCommandMap = classCraftServer.findMethodByReturnType(SimpleCommandMap.class);
    static SimpleCommandMap commandMap = getCommandMap.of(Bukkit.getServer()).call();
    static RefField fieldKnownCommands = getRefClass(SimpleCommandMap.class).getField("knownCommands");

    static Map getKnownCommands() {
        return (Map) fieldKnownCommands.of(commandMap).get();
    }

    private HashMap<String, PluginCommand> pluginCommands = new HashMap<>();

    /**
     * Create new plugin command
     *
     * @param name command name
     * @return PluginCommand or null if command already exists
     */
    public PluginCommand addCommand(String name) {
        PluginCommand command = Bukkit.getPluginCommand(name);
        if (command != null) return null;
        command = pluginCommandConstructor.create(name, plugin);
        commandMap.register(plugin.getName(), command);
        pluginCommands.put(name, command);
        return command;
    }

    /**
     * Create new plugin command and append executor
     *
     * @param name command name
     * @return true if command is successfully created
     */
    public boolean addCommand(String name, CommandExecutor executor) {
        PluginCommand command = addCommand(name);
        if (command == null) return false;
        command.setExecutor(executor);
        return true;
    }

    /**
     * unregister plugin command by name
     *
     * @param name command name
     * @return true if command is successfully unregistered
     */
    public boolean stopCommand(String name) {
        PluginCommand command = pluginCommands.remove(name);
        if (command == null) return false;
        boolean result = command.unregister(commandMap);
        getKnownCommands().remove(name);
        return result;
    }

    private HashMap<Long, Runnable> finishers = new HashMap<>();
    long nextFinisher = 0;
    boolean blockFinishers = false;

    /**
     * Register a new task, which is executed when the workspace stops.
     * this happens on {@link #stop()}, on reload workspace, on server stop/restart.
     *
     * @param task task to be run
     * @return id of task
     */
    public synchronized long addFinisher(Runnable task) {
        if (status != LOADING && status != LOADED) throw new WorkspaceStateException();
        finishers.put(nextFinisher, task);
        return nextFinisher++;
    }

    /**
     * cancel execution of task registered with {@link #addFinisher(Runnable)}
     *
     * @param id id of task
     * @return true if task is cancelled
     */
    public synchronized boolean stopFinisher(long id) {
        if (status != LOADING && status != LOADED) return false;
        return finishers.remove(id) != null;
    }

    /**
     * Run code synchronized on object<br/>
     * This method does not provide execution in Bukkit main thread. See {@link #runTask(Runnable)}
     *
     * @param callable callable task
     * @param sync     object, which will be synchronized
     * @param <T>      callable return value
     * @return result of callable
     * @throws Exception if callable throws
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public <T> T synchronize(Callable<T> callable, Object sync) throws Exception {
        synchronized (sync) {
            return callable.call();
        }
    }

    /**
     * Run code synchronized on this workspace.
     * This method does not provide execution in Bukkit main thread. See {@link #runTask(Runnable)}
     *
     * @param callable callable task
     * @param <T>      callable return value
     * @return result of callable
     * @throws Exception if callable throws
     */
    public synchronized <T> T synchronize(Callable<T> callable) throws Exception {
        return callable.call();
    }

    @Override
    public String toString() {
        return "Workspace{" + getName() + "}";
    }

    private String getStatusAsString() {
        switch (status) {
            case CREATED:
                return "CREATED";
            case ERROR:
                return "ERROR";
            case LOADED:
                return "LOADED";
            case LOADING:
                return "LOADING";
            case UNLOADED:
                return "UNLOADED";
            default:
                return "UNKNOWN_STATUS";
        }
    }

}
