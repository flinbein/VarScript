package ru.dpohvar.varscript.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

public class WorkspaceCommandExecutor implements CommandExecutor{

    private final VarScript plugin;

    public WorkspaceCommandExecutor(VarScript plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Caller caller = plugin.getCallerService().getCaller(sender);
        if (strings.length == 0) return onCommandEmpty(caller);

        if (strings.length == 1 && strings[0].equals("list")) return onCommandList(caller);
        if (strings.length == 1 && strings[0].equals("remove")) return onCommandRemove(caller, null);
        if (strings.length == 1 && strings[0].equals("reload")) return onCommandReload(caller, null);
        if (strings.length == 1 && strings[0].equals("create")) return onCommandCreate(caller, null);
        if (strings.length == 1 && strings[0].equals("stop")) return onCommandStop(caller, null);

        if (strings.length == 2 && strings[0].equals("set")) return onCommandSet(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("remove")) return onCommandRemove(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("create")) return onCommandCreate(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("reload")) return onCommandReload(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("stop")) return onCommandStop(caller, strings[1]);
        return false;
    }

    private boolean onCommandStop(Caller caller, String workspaceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (workspaceName == null) workspaceName = callerWorkspaceName;
        Workspace workspace = service.getWorkspace(workspaceName);
        if (workspace == null) {
            caller.sendErrorMessage("workspace "+workspaceName+" is not exists", callerWorkspaceName);
        } else {
            workspace.stopTriggers();
            caller.sendMessage("workspace " + workspaceName + " is stopped", callerWorkspaceName);
        }
        return true;
    }

    private boolean onCommandCreate(Caller caller, String workspaceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (workspaceName == null) workspaceName = callerWorkspaceName;
        Workspace workspace = service.getWorkspace(workspaceName);
        if (workspace != null) {
            caller.sendErrorMessage("workspace "+workspaceName+" is already exists", callerWorkspaceName);
        } else {
            service.getOrCreateWorkspace(workspaceName);
            caller.sendMessage("workspace " + workspaceName + " is created", callerWorkspaceName);
        }
        return true;
    }

    private boolean onCommandRemove(Caller caller, String workspaceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (workspaceName == null) workspaceName = callerWorkspaceName;
        Workspace workspace = service.getWorkspace(workspaceName);
        if (workspace == null) {
            caller.sendErrorMessage("workspace "+workspaceName+" is not exists", callerWorkspaceName);
        } else {
            workspace.removeWorkspace();
            caller.sendMessage("workspace " + workspaceName + " is removed", callerWorkspaceName);
        }
        return true;
    }

    private boolean onCommandReload(Caller caller, String workspaceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (workspaceName == null) workspaceName = callerWorkspaceName;
        Workspace workspace = service.getWorkspace(workspaceName);
        if (workspace != null) workspace.removeWorkspace();
        service.getOrCreateWorkspace(workspaceName);
        if (workspace == null) {
            caller.sendMessage("workspace "+workspaceName+" is created", workspaceName);
        } else {
            caller.sendMessage("workspace "+workspaceName+" is reloaded", workspaceName);
        }
        return true;
    }

    public boolean onCommandEmpty(Caller caller) {
        WorkspaceService service = plugin.getWorkspaceService();
        String workspaceName = service.getWorkspaceName(caller.getSender());
        Workspace workspace = service.getWorkspace(workspaceName);
        if (workspace == null) caller.sendMessage("workspace is disabled", workspaceName);
        else caller.sendMessage("workspace is active", workspaceName);
        return true;
    }

    public boolean onCommandList(Caller caller) {
        WorkspaceService service = plugin.getWorkspaceService();
        String workspaceName = service.getWorkspaceName(caller.getSender());
        Workspace[] workspaces = service.getWorkspaces();
        if (workspaces.length == 0) {
            caller.sendMessage("no active workspaces", workspaceName);
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("active workspaces: ");
            builder.append(workspaces.length);
            for (Workspace workspace : workspaces) {
                builder.append('\n').append(workspace.getName());
            }
            caller.sendMessage(builder.toString(), workspaceName);
        }
        return true;
    }

    public boolean onCommandSet(Caller caller, String newName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String workspaceName = service.getWorkspaceName(caller.getSender());
        service.setWorkspaceName(caller.getSender(), newName);
        caller.sendMessage("new workspace set: " + newName, workspaceName);
        return true;
    }


}
