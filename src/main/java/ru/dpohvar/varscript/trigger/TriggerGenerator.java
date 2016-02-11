package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.SimpleType;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.List;

public interface TriggerGenerator {

    public void stopTriggers();

    public int triggerCount();

    public Workspace getWorkspace();

    public Trigger[] getTriggers();

    public boolean isDisabled();

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ic);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ic, @ClosureParams(FirstParam.FirstGenericType.class) Closure handler);

    public BukkitEventTrigger listen(Closure closure);

    public BukkitEventTrigger listen(EventPriority priority, Closure closure);

    public TriggerGenerator generator();

    public BukkitTimeoutTrigger timeout(long timeout, boolean sync);

    public BukkitTimeoutTrigger timeout(long timeout);

    public BukkitIntervalTrigger interval(long interval, long timeout, boolean sync);

    public BukkitIntervalTrigger interval(long interval, long timeout);

    public BukkitIntervalTrigger interval(long interval);

    public BukkitTimeoutTrigger timeout(long timeout, boolean sync, Closure handler);

    public BukkitTimeoutTrigger timeout(long timeout, Closure handler);

    public BukkitIntervalTrigger interval(long interval, long timeout, boolean sync, Closure handler);

    public BukkitIntervalTrigger interval(long interval, long timeout, Closure handler);

    public BukkitIntervalTrigger interval(long interval, Closure handler);

    public StopHookTrigger stopHook();

    public StopHookTrigger stopHook(Closure closure);

    public CommandTrigger command(String name, String description, String usage, List<String> aliases);

    public CommandTrigger command(String name, String description, String usage, List<String> aliases, @ClosureParams(value=SimpleType.class,options={"org.bukkit.command.CommandSender","List<String>","String"}) Closure handler);

    public CommandTrigger command(Object params);

    public TriggerGenerator getParent();
}
