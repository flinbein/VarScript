package ru.dpohvar.varscript.trigger;

import groovy.lang.Closure;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.Set;

public class StopHookTrigger implements Trigger, Runnable {

    private boolean stopped;
    private Closure handler;
    private final Workspace workspace;
    private final Set<Trigger> parentTriggers;

    public StopHookTrigger(Workspace workspace, Set<Trigger> parentTriggers){
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

    public StopHookTrigger call(Closure closure){
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
        if (handler != null) try {
            handler.call();
        } catch (Throwable t) {
            Caller caller = workspace.getWorkspaceService().getVarScript().getCallerService().getConsoleCaller();
            caller.sendThrowable(t, workspace.getName());
        }
    }
}














