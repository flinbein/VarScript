package ru.dpohvar.varscript.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.command.GroovyLinePrompt;
import ru.dpohvar.varscript.workspace.Workspace;

public class CompileScriptEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Caller caller;
    private boolean cancelled;
    private Workspace workspace;
    private String script;

    public CompileScriptEvent(Caller caller, String script, Workspace workspace) {
        this.caller = caller;
        this.script = script;
        this.workspace = workspace;
    }

    public Caller getCaller() {
        return caller;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
