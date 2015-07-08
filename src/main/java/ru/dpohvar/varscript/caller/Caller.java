package ru.dpohvar.varscript.caller;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.util.logging.Level;

import static org.bukkit.ChatColor.*;

public class Caller {

    CommandSender sender;
    private final CallerService service;
    private final VarScript plugin;

    private Object lastResult;

    public Caller(CallerService service, CommandSender sender){
        this.service = service;
        this.sender = sender;
        plugin = service.getPlugin();
    }

    public Object getLastResult() {
        return lastResult;
    }

    public void setLastResult(Object lastResult) {
        this.lastResult = lastResult;
    }

    public CallerService getService() {
        return service;
    }

    public CommandSender getSender() {
        return sender;
    }

    public void sendPrintMessage(CharSequence message, String source){
        if (source == null) source = "";
        String prefix = String.format(VarScript.printPrefix, source);
        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            ((Conversable) sender).sendRawMessage(prefix + message);
        } else {
            sender.sendMessage(prefix + message);
        }
    }

    public void sendMessage(CharSequence message, String source){
        if (source == null) source = "";
        String prefix = String.format(VarScript.prefix, source);
        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            ((Conversable) sender).sendRawMessage(prefix + message);
        } else {
            sender.sendMessage(prefix + message);
        }
    }

    public void sendErrorMessage(CharSequence message, String source){
        if (source == null) source = "";
        String prefix = String.format(VarScript.errorPrefix, source);
        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            ((Conversable) sender).sendRawMessage(prefix + message);
        } else {
            sender.sendMessage(prefix + message);
        }
    }

    public void sendThrowable(Throwable e, String source){
        String message = e.getLocalizedMessage();
        if (message == null) message = e.getMessage();
        if (message == null) message = e.toString();
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().log(Level.WARNING, source, e);
        }
        sendErrorMessage(RED + e.getClass().getSimpleName() + RESET + "\n" + message, source);
    }

    public Workspace getCurrentWorkspace(){
        WorkspaceService workspaceService = plugin.getWorkspaceService();
        String workspaceName = workspaceService.getWorkspaceName(sender);
        return workspaceService.getOrCreateWorkspace(workspaceName);
    }

    @Override
    public String toString() {
        return sender.getName()+"("+sender.getClass().getSimpleName()+")";
    }
}
