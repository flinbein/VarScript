package ru.dpohvar.varscript.utils;

import org.bukkit.Server;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

public class PreparedScriptProperties implements ScriptProperties {

    private Caller caller;
    private Workspace workspace;
    private Server server;

    public void setCaller(Caller caller) {
        this.caller = caller;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public Caller getCaller() {
        return null;
    }

    @Override
    public Workspace getWorkspace() {
        return null;
    }

    @Override
    public Object getMe(){
        CommandSender sender = caller.getSender();
        if (sender instanceof BlockCommandSender) return ((BlockCommandSender) sender).getBlock();
        else return sender;
    }

    public Object get_(){
        return caller.getLastResult();
    }

    @Override
    public Server getServer() {
        return server;
    }

    public WorkspaceService getGlobal(){
        return workspace.getWorkspaceService();
    }
}
