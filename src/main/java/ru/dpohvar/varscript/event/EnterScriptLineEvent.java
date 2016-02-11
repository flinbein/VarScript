package ru.dpohvar.varscript.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.dpohvar.varscript.command.GroovyLinePrompt;

import java.util.List;

public class EnterScriptLineEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final CommandSender commandSender;
    private boolean cancelled;
    private String line;
    private List<String> buffer;
    private GroovyLinePrompt.LineInputAction inputAction;

    public EnterScriptLineEvent(CommandSender commandSender, String line, List<String> buffer, GroovyLinePrompt.LineInputAction inputAction) {
        this.commandSender = commandSender;
        this.line = line;
        this.inputAction = inputAction;
    }

    public CommandSender getCommandSender() {
        return commandSender;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public List<String> getBuffer() {
        return buffer;
    }

    public GroovyLinePrompt.LineInputAction getInputAction() {
        return inputAction;
    }

    public void setInputAction(GroovyLinePrompt.LineInputAction inputAction) {
        this.inputAction = inputAction;
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
