package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.SimpleType;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.*;

public class TriggerContainer implements Trigger, TriggerGenerator, TriggerHolder {

    private boolean disabled;
    private final Workspace workspace;
    private final Set<Trigger> parentTriggers;
    private final TriggerHolder parent;
    Set<Trigger> triggers = new LinkedHashSet<Trigger>();

    public TriggerContainer(Workspace workspace, TriggerHolder parent, Set<Trigger> parentTriggers){
        this.workspace = workspace;
        this.parentTriggers = parentTriggers;
        this.parent = parent;
        parentTriggers.add(this);
    }

    @Override
    public TriggerHolder getParent() {
        return parent;
    }

    @Override
    public Trigger[] getTriggers() {
        Trigger[] result = new Trigger[triggers.size()];
        return triggers.toArray(result);
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean isStopped() {
        return disabled;
    }

    @Override
    public boolean stop() {
        if (disabled) return false;
        disabled = true;
        if (parentTriggers != null) parentTriggers.remove(this);
        for (Trigger trigger : getTriggers()) {
            trigger.stop();
            if (trigger instanceof StopHookTrigger) try {
                ((StopHookTrigger) trigger).run();
            } catch (Exception e) {
                Caller caller = workspace.getWorkspaceService().getVarScript().getCallerService().getConsoleCaller();
                caller.sendThrowable(e, workspace.getName());
            }
        }
        return true;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass){
        return listen(eventClass, EventPriority.NORMAL, false);
    }


    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority){
        return listen(eventClass, priority, false);
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ic){
        if (disabled) throw new IllegalStateException("container is disabled");
        return new BukkitEventTrigger<T>(workspace, triggers, eventClass, priority, ic);
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler){
        return listen(eventClass, EventPriority.NORMAL, false, handler);
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler){
        return listen(eventClass, priority, false, handler);
    }

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ic, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler){
        Class<?>[] types = handler.getParameterTypes();
        if (types.length > 1) throw new IllegalArgumentException("wrong number of closure params: "+types.length);
        if (types.length == 1 && !types[0].isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException("wrong type of closure param: "+types[0]+", expected: "+eventClass);
        }
        return listen(eventClass, priority, ic).call(handler);
    }

    public BukkitEventTrigger listen(Closure closure){
        return listen(EventPriority.NORMAL, closure);
    }

    public BukkitEventTrigger listen(EventPriority priority, Closure closure){
        if (disabled) throw new IllegalStateException("container is disabled");
        Class<?>[] types = closure.getParameterTypes();
        if (types.length != 1) throw new IllegalArgumentException("wrong number of closure params: "+types.length);
        if (!Event.class.isAssignableFrom(types[0])) {
            throw new IllegalArgumentException("wrong type of closure param: "+types[0].getName()+", expected: "+Event.class.getName());
        }
        Class<? extends Event> eventClass = types[0].asSubclass(Event.class);
        BukkitEventTrigger trigger = new BukkitEventTrigger<Event>(workspace, triggers, eventClass, priority, false);
        trigger.setHandler(closure);
        return trigger;
    }

    @Override
    public BukkitTimeoutTrigger timeout(long timeout, boolean sync){
        if (disabled) throw new IllegalStateException("container is disabled");
        return new BukkitTimeoutTrigger(workspace, triggers, timeout, sync);
    }

    @Override
    public BukkitTimeoutTrigger timeout(long timeout){
        return timeout(timeout, true);
    }

    @Override
    public BukkitIntervalTrigger interval(long interval, long timeout, boolean sync){
        if (disabled) throw new IllegalStateException("container is disabled");
        return new BukkitIntervalTrigger(workspace, triggers, timeout, interval, sync);
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
        return new StopHookTrigger(workspace, triggers);
    }

    @Override
    public StopHookTrigger stopHook(Closure closure){
        return stopHook().call(closure);
    }

    @Override
    public TriggerContainer generator() {
        return new TriggerContainer(workspace, this, parentTriggers);
    }

    @Override
    public void stopTriggers() {
        stop();
    }

    @Override
    public int triggerCount() {
        return triggers.size();
    }

    @Override
    public CommandTrigger command(String name, String description, String usage, List<String> aliases){
        if (name == null) throw new IllegalArgumentException("command name is required");
        if (disabled) throw new IllegalStateException("container is disabled");
        if (description == null) description = "Command generated by VarScript. Workspace: "+name;
        if (usage == null) usage = "";
        if (aliases == null) aliases = new ArrayList<String>();
        return new CommandTrigger(workspace, triggers, name, description, usage, aliases);
    }

    @Override
    public CommandTrigger command(String name, String description, String usage, List<String> aliases, @ClosureParams(value=SimpleType.class,options={"org.bukkit.command.CommandSender","List<String>","String"}) Closure handler){
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
}
