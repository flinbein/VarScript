package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;
import org.codehaus.groovy.runtime.callsite.AbstractCallSite;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.workspace.Workspace;

import java.lang.reflect.Method;
import java.util.Set;

public class BukkitEventTrigger<T extends Event> implements Trigger, Listener, EventExecutor {

    private boolean stopped;
    private Closure handler;
    private boolean useBinding;
    private final Workspace workspace;
    private final Class eventClass;
    private final Set<Trigger> parentTriggers;
    private final HandlerList handlerList;
    private final RegisteredListener registeredListener;

    public BukkitEventTrigger(Workspace workspace, Set<Trigger> parentTriggers, Class<T> eventClass, EventPriority priority, boolean ignoreCancelled){
        this.workspace = workspace;
        this.eventClass = eventClass;
        this.parentTriggers = parentTriggers;
        Class<? extends Event> registrationClass = getRegistrationClass(eventClass);
        handlerList = getHandlerList(registrationClass);
        boolean useTimings = Bukkit.getServer().getPluginManager().useTimings();
        VarScript plugin = workspace.getWorkspaceService().getVarScript();
        if (useTimings) {
            registeredListener = new TimedRegisteredListener(this, this, priority, plugin, ignoreCancelled);
        } else {
            registeredListener = new RegisteredListener(this, this, priority, plugin, ignoreCancelled);
        }
        handlerList.register(registeredListener);
        parentTriggers.add(this);
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (handler == null) return;
        try {
            if (!eventClass.isInstance(event)) return;
            if (useBinding) {
                handler.setProperty("event",event);
                handler.call();
            } else {
                handler.call(event);
            }
        } catch (Throwable t) {
            throw new EventException(t);
        }
    }

    public Closure getHandler() {
        return handler;
    }

    public void setHandler(Closure handler) {
        Class[] types = handler.getParameterTypes();
        if (types.length > 1) throw new IllegalArgumentException("wrong number of closure params: "+types.length);
        if (types.length == 1 && !types[0].isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException("wrong type of closure param: "+types[0]+", expected: "+eventClass);
        }
        if (types.length == 0) useBinding = true;
        this.handler = handler;
    }

    public BukkitEventTrigger<T> call(Closure closure){
        setHandler(closure);
        return this;
    }

    public Class getEventClass() {
        return eventClass;
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
        handlerList.unregister(registeredListener);
        this.stopped = true;
        if (parentTriggers != null) parentTriggers.remove(this);
        return true;
    }

    private static Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
            }
        }
    }

    private static HandlerList getHandlerList(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }
}














