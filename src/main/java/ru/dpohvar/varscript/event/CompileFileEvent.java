package ru.dpohvar.varscript.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;

import java.io.File;

public class CompileFileEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Caller caller;
    private boolean cancelled;
    private Workspace workspace;
    private File file;

    public CompileFileEvent(Caller caller, File file, Workspace workspace) {
        this.caller = caller;
        this.file = file;
        this.workspace = workspace;
    }

    public Caller getCaller() {
        return caller;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
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
