package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.Set;

public class BukkitIntervalTrigger implements Trigger, Runnable {

    private boolean stopped;
    private Closure handler;
    private int usedArgs;
    private final Workspace workspace;
    private final Set<Trigger> parentTriggers;
    private final BukkitTask bukkitTask;
    private final long timeout;
    private final long interval;
    private final boolean sync;
    private int count;

    public BukkitIntervalTrigger(Workspace workspace, Set<Trigger> parentTriggers, long timeout, long interval, boolean sync){
        this.workspace = workspace;
        this.parentTriggers = parentTriggers;
        this.timeout = timeout;
        this.interval = interval;
        this.sync = sync;
        VarScript plugin = this.workspace.getWorkspaceService().getVarScript();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        parentTriggers.add(this);
        if (sync) bukkitTask = scheduler.runTaskTimer(plugin, this, timeout, interval);
        else bukkitTask = scheduler.runTaskTimerAsynchronously(plugin, this, timeout, interval);
    }

    public long getInterval() {
        return interval;
    }

    public boolean isSync() {
        return sync;
    }

    public long getCount() {
        return count;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public void run() {
        if (handler == null) return;
        int counter = count++;
        try {
            if (usedArgs == 0) {
                handler.setProperty("count", counter);
                handler.setProperty("trigger", this);
                handler.call();
            } else if (usedArgs == 1) {
                handler.call(counter);
            } else if (usedArgs == 2) {
                handler.call(counter, this);
            }
        } catch (Throwable t) {
            Caller caller = workspace.getWorkspaceService().getVarScript().getCallerService().getConsoleCaller();
            caller.sendThrowable(t, workspace.getName());
        }

    }

    public Closure getHandler() {
        return handler;
    }

    public void setHandler(Closure handler) {
        Class[] types = handler.getParameterTypes();
        if (types.length > 2) throw new IllegalArgumentException("wrong number of closure params: "+types.length);
        int usedArgs = 0;
        if (types.length > 0) {
            if (!types[0].isAssignableFrom(Integer.class) && types[0] != int.class) {
                throw new IllegalArgumentException("wrong type of closure param: "+types[0]+", expected: "+Integer.class);
            }
            usedArgs = 1;
        }
        if (types.length > 1) {
            if (!types[1].isAssignableFrom(BukkitIntervalTrigger.class)) {
                throw new IllegalArgumentException("wrong type of closure param: "+types[1]+", expected: "+BukkitIntervalTrigger.class);
            }
            usedArgs = 2;
        }
        this.usedArgs = usedArgs;
        this.handler = handler;
    }

    public BukkitIntervalTrigger call(Closure handler){
        setHandler(handler);
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
        if (stopped) return false;
        bukkitTask.cancel();
        stopped = true;
        if (parentTriggers != null) parentTriggers.remove(this);
        return true;
    }
}














