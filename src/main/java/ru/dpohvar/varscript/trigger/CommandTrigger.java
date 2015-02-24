package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.plugin.*;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.dpohvar.varscript.workspace.Workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static ru.dpohvar.varscript.utils.ReflectionUtils.*;

import static ru.dpohvar.varscript.utils.ReflectionUtils.getRefClass;

public class CommandTrigger extends Command implements Trigger {

    private boolean stopped;
    private Closure handler;
    private final Workspace workspace;
    private final Set<Trigger> parentTriggers;
    private final String name;
    private List<Integer> argOrder;
    private final SimpleCommandMap commandMap;

    public CommandTrigger(Workspace workspace, Set<Trigger> parentTriggers, String name, String description, String usage, List<String> aliases){
        super(name, description, usage, aliases);
        this.workspace = workspace;
        this.parentTriggers = parentTriggers;
        this.name = name;
        Server server = workspace.getWorkspaceService().getVarScript().getServer();
        this.commandMap = mGetCommandMap.of(server).call();
        commandMap.register(workspace.getName(), this);
        parentTriggers.add(this);
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
                List<Object> passArguments = new ArrayList<Object>();
                for (Integer t : argOrder) switch (t) {
                    case 0: passArguments.add(sender);
                    case 1: passArguments.add(args);
                    case 2: passArguments.add(command);
                }
                Object result = handler.call(passArguments.toArray());
                return DefaultGroovyMethods.asBoolean(result);
            }
        } catch (Throwable e) {
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
        return true;
    }

    static RefClass<PluginCommand> cPluginCommand = getRefClass(PluginCommand.class);
    static RefConstructor<PluginCommand> nPluginCommand = cPluginCommand.getConstructor(String.class, Plugin.class);
    static RefClass<?> cCraftServer = getRefClass("{cb}.CraftServer");
    static RefMethod<SimpleCommandMap> mGetCommandMap = cCraftServer.findMethodByReturnType(SimpleCommandMap.class);
    static RefField fKnownCommands = getRefClass(SimpleCommandMap.class).getField("knownCommands");


}














