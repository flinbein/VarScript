package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.List;

public interface TriggerGenerator {

    public boolean isDisabled();

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ic);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, Closure handler);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, Closure handler);

    public <T extends Event> BukkitEventTrigger<T> listen(Class<T> eventClass, EventPriority priority, boolean ic, Closure handler);

    public BukkitEventTrigger listen(Closure closure);

    public BukkitEventTrigger listen(EventPriority priority, Closure closure);

    public TriggerContainer generator();

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

    public CommandTrigger command(String name, String description, String usage, List<String> aliases, Closure handler);

    public CommandTrigger command(Object params);

    public TriggerHolder getParent();
}
