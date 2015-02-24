package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.Set;

public class ShutdownHookTrigger implements Trigger, Runnable {

    private boolean stopped;
    private Closure handler;
    private final Workspace workspace;
    private final Set<Trigger> parentTriggers;

    public ShutdownHookTrigger(Workspace workspace, Set<Trigger> parentTriggers){
        this.workspace = workspace;
        this.parentTriggers = parentTriggers;
        parentTriggers.add(this);
    }

    public Closure getHandler() {
        return handler;
    }

    public void setHandler(Closure handler) {
        this.handler = handler;
    }

    public ShutdownHookTrigger call(Closure closure){
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
        this.stopped = true;
        if (parentTriggers != null) parentTriggers.remove(this);
        return true;
    }

    @Override
    public void run() {
        if (handler != null) handler.call();
    }
}














