package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.Set;

public class BukkitTimeoutTrigger implements Trigger, Runnable {

    private boolean stopped;
    private Closure handler;
    private final Workspace workspace;
    private final Set<Trigger> parentTriggers;
    private final BukkitTask bukkitTask;
    private final long timeout;
    private final boolean sync;

    public BukkitTimeoutTrigger(Workspace workspace, Set<Trigger> parentTriggers, long timeout, boolean sync){
        this.workspace = workspace;
        this.parentTriggers = parentTriggers;
        this.timeout = timeout;
        this.sync = sync;
        VarScript plugin = this.workspace.getWorkspaceService().getVarScript();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        parentTriggers.add(this);
        if (sync) bukkitTask = scheduler.runTaskLater(plugin, this, timeout);
        else bukkitTask = scheduler.runTaskLaterAsynchronously(plugin, this, timeout);

    }

    public long getTimeout() {
        return timeout;
    }

    public boolean isSync() {
        return sync;
    }

    @Override
    public void run() {
        stop();
        if (handler != null) handler.run();
    }

    public Closure getHandler() {
        return handler;
    }

    public void setHandler(Closure handler) {
        this.handler = handler;
    }

    public BukkitTimeoutTrigger call(Closure handler){
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














