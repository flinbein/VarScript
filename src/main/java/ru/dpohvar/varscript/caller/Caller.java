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
    private final BukkitScheduler scheduler;

    private Object lastResult;

    public Caller(CallerService service, CommandSender sender){
        this.service = service;
        this.sender = sender;
        plugin = service.getPlugin();
        scheduler = plugin.getServer().getScheduler();
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

    private StringBuilder printCache = new StringBuilder();
    private BukkitTask flushBukkitTask = null;

    public void sendPrintMessage(String message, String source){
        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            ((Conversable) sender).sendRawMessage(VarScript.printPrefix + message);
        } else {
            if (source == null) source = "";
            String prefix = String.format(VarScript.printPrefix, source);
            sender.sendMessage(prefix + message);
        }
    }

    public void sendMessage(String message, String source){
        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            ((Conversable) sender).sendRawMessage(VarScript.prefix + message);
        } else {
            if (source == null) source = "";
            String prefix = String.format(VarScript.prefix, source);
            sender.sendMessage(prefix + message);
        }
    }

    public void sendErrorMessage(String message, String source){
        if (sender instanceof Conversable && ((Conversable) sender).isConversing()) {
            ((Conversable) sender).sendRawMessage(VarScript.errorPrefix + message);
        } else {
            if (source == null) source = "";
            String prefix = String.format(VarScript.errorPrefix, source);
            sender.sendMessage(prefix + message);
        }
    }

    public void sendThrowable(Throwable e, String source){
        String message = e.getLocalizedMessage();
        if (message == null) message = e.getMessage();
        if (message == null) message = e.toString();
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().log(Level.WARNING, e.getClass().getSimpleName(), e);
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
