package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static org.bukkit.ChatColor.stripColor;
import static ru.dpohvar.varscript.utils.ReflectionUtils.RefField;
import static ru.dpohvar.varscript.utils.ReflectionUtils.getRefClass;

public class CommandTrigger extends Command implements Trigger {

    private boolean stopped;
    private Closure<?> handler;
    private final Workspace workspace;
    private final Set<Trigger> parentTriggers;
    private final String name;
    private final String fallbackPrefix;
    private List<Integer> argOrder;
    private final SimpleCommandMap commandMap;
    private static Field commandMapField;

    static {
        try {
            commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
        } catch (NoSuchFieldException error) {
            commandMapField = null;
        }
    }

    public CommandTrigger(Workspace workspace, Set<Trigger> parentTriggers, String name, String description, String usage, List<String> aliases) {
        super(name, description, usage, aliases);
        VarScript varScript = workspace.getWorkspaceService().getVarScript();
        var server = varScript.getServer();
        var pluginManager = (SimplePluginManager) server.getPluginManager();
        this.workspace = workspace;
        this.parentTriggers = parentTriggers;
        this.name = name;
        this.fallbackPrefix = varScript.getName() + ":" + stripColor(workspace.getName()).toLowerCase().trim().replace(' ', '_');
        try {
            this.commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        commandMap.register(fallbackPrefix, this);
        parentTriggers.add(this);
    }

    public String getFallbackPrefix() {
        return fallbackPrefix;
    }

    public Closure getHandler() {
        return handler;
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] strings) {
        if (handler == null) return true;
        List<String> args = Arrays.asList(strings);
        try {
            if (argOrder.isEmpty()) {
                handler.setProperty("sender",sender);
                handler.setProperty("command",command);
                handler.setProperty("args",args);
                Object result = handler.call();
                return DefaultGroovyMethods.asBoolean(result);
            } else {
                List<Object> passArguments = new ArrayList<>();
                for (Integer t : argOrder) switch (t) {
                    case 0: passArguments.add(sender); break;
                    case 1: passArguments.add(args); break;
                    case 2: passArguments.add(command); break;
                }
                Object result = handler.call(passArguments.toArray());
                return DefaultGroovyMethods.asBoolean(result);
            }
        } catch (Throwable e) {
            Caller caller = workspace.getWorkspaceService().getVarScript().getCallerService().getConsoleCaller();
            caller.sendThrowable(e, workspace.getName());
            String className = e.getClass().getName();
            String commandName = workspace.getName()+":"+name;
            sender.sendMessage(className + " on command " + commandName + "\n" + e.getMessage());
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public void setHandler(String script){
        File scriptsDirectory = workspace.getWorkspaceService().getScriptsDirectory();
        File scriptFile = new File(scriptsDirectory, script);
        CommandScriptClosure scriptClosure = new CommandScriptClosure(workspace);
        scriptClosure.setScript(scriptFile);
        setHandler(scriptClosure);
    }

    public void setHandler(Closure handler) {
        Class[] types = handler.getParameterTypes();
        List<Class<?>> inject = Arrays.asList(CommandSender.class, List.class, String.class);
        List<Integer> argOrder = new ArrayList<Integer>();
        scanClasses: for (Class type : types) {
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
        this.argOrder = argOrder;
        this.handler = handler;
    }

    public CommandTrigger call(Closure closure){
        setHandler(closure);
        return this;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public boolean stop() {
        if (this.stopped) return false;
        unregister(commandMap);
        this.stopped = true;
        if (parentTriggers != null) parentTriggers.remove(this);
        Map<?,?> knownCommands = fKnownCommands.of(commandMap).get();
        knownCommands.entrySet().removeIf(entry -> entry.getValue() == this);
        return true;
    }

    static RefField<Map> fKnownCommands = getRefClass(SimpleCommandMap.class).findField(Map.class);


}














