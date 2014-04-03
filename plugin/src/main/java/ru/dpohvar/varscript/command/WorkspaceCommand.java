package ru.dpohvar.varscript.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.VarScriptPlugin;
import ru.dpohvar.varscript.Workspace;
import ru.dpohvar.varscript.WorkspaceManager;

import java.util.List;
import java.util.Set;

import static org.bukkit.ChatColor.*;

/**
 * Executor of command /workspace
 */
public class WorkspaceCommand implements CommandExecutor {

    private WorkspaceManager manager;

    public WorkspaceCommand(WorkspaceManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) return showWorkspace(commandSender, manager, commandSender.getName());
        String action = strings[0];
        if (action.equalsIgnoreCase("list")) {
            String pattern = "";
            if (strings.length >= 2) pattern = strings[1];
            return showWorkspaces(commandSender, manager, pattern);
        } else if (action.equalsIgnoreCase("get")) {
            if (strings.length < 2) return showWorkspace(commandSender, manager, commandSender.getName());
            return showWorkspace(commandSender, manager, strings[1]);
        } else if (action.equalsIgnoreCase("set")) {
            String wsName, user = commandSender.getName();
            if (strings.length < 2) return false;
            wsName = strings[1];
            if (strings.length > 2) user = strings[2];
            return setWorkspace(commandSender, manager, wsName, user);
        } else if (action.equalsIgnoreCase("set-default")) {
            String wsName, user = commandSender.getName();
            if (strings.length < 2) return false;
            wsName = strings[1];
            if (strings.length > 2) user = strings[2];
            return setDefaultWorkspace(commandSender, manager, wsName, user);
        } else if (action.equalsIgnoreCase("stop")) {
            String wsName = null;
            if (strings.length > 1) wsName = strings[1];
            return stopWorkspace(commandSender, manager, wsName);
        } else if (action.equalsIgnoreCase("load")) {
            String wsName = null;
            if (strings.length > 1) wsName = strings[1];
            return loadWorkspace(commandSender, manager, wsName);
        } else if (action.equalsIgnoreCase("unload")) {
            String wsName = null;
            if (strings.length > 1) wsName = strings[1];
            return unloadWorkspace(commandSender, manager, wsName);
        } else if (action.equalsIgnoreCase("reload")) {
            String wsName = null;
            if (strings.length > 1) wsName = strings[1];
            return reloadWorkspace(commandSender, manager, wsName);
        } else if (action.equalsIgnoreCase("reload-all")) {
            //noinspection SimplifiableIfStatement
            if (strings.length > 1) return false;
            return reloadAllWorkspaces(commandSender, manager);
        } else if (action.equalsIgnoreCase("load-all")) {
            //noinspection SimplifiableIfStatement
            if (strings.length > 1) return false;
            return loadAllWorkspaces(commandSender, manager);
        } else if (action.equalsIgnoreCase("unload-all")) {
            //noinspection SimplifiableIfStatement
            if (strings.length > 1) return false;
            return unloadAllWorkspaces(commandSender, manager);
        } else {
            return false;
        }
    }

    public static boolean reloadWorkspace(CommandSender commandSender, WorkspaceManager manager, String wsName) {
        if (wsName == null) wsName = manager.getWorkspaceName(commandSender.getName());

        manager.unloadWorkspace(wsName);
        Workspace workspace = manager.getWorkspace(wsName);
        if (workspace == null) {
            commandSender.sendMessage(
                    VarScriptPlugin.prefix + "can not load workspace " + GRAY + wsName
            );
        } else {
            commandSender.sendMessage(
                    VarScriptPlugin.prefix + "workspace " + GREEN + wsName + RESET + " reloaded"
            );
        }
        return true;
    }

    public static boolean reloadAllWorkspaces(CommandSender commandSender, WorkspaceManager manager) {
        manager.unloadAllWorkspaces();
        manager.loadAllWorkspaces();
        commandSender.sendMessage(
                VarScriptPlugin.prefix + "workspaces reloaded"
        );
        return true;
    }

    public static boolean loadAllWorkspaces(CommandSender commandSender, WorkspaceManager manager) {
        manager.loadAllWorkspaces();
        commandSender.sendMessage(
                VarScriptPlugin.prefix + "workspaces loaded"
        );
        return true;
    }

    public static boolean unloadAllWorkspaces(CommandSender commandSender, WorkspaceManager manager) {
        manager.unloadAllWorkspaces();
        commandSender.sendMessage(
                VarScriptPlugin.prefix + "workspaces unloaded"
        );
        return true;
    }

    public static boolean showWorkspace(CommandSender commandSender, WorkspaceManager manager, String user) {
        String wsName = manager.getWorkspaceName(user);
        boolean loaded = manager.isWorkspaceLoaded(wsName);
        commandSender.sendMessage(
                VarScriptPlugin.prefix + AQUA + user + RESET + " -> " + (loaded ? GREEN : GRAY) + wsName
        );
        return true;
    }

    public static boolean stopWorkspace(CommandSender commandSender, WorkspaceManager manager, String wsName) {
        if (wsName == null) wsName = manager.getWorkspaceName(commandSender.getName());
        if (manager.isWorkspaceLoaded(wsName)) {
            Workspace workspace = manager.getWorkspace(wsName);
            workspace.stop();
            commandSender.sendMessage(
                    VarScriptPlugin.prefix + GREEN + wsName + RESET + " stopped"
            );
        } else {
            commandSender.sendMessage(
                    VarScriptPlugin.prefix + GRAY + wsName + RESET + " is not loaded"
            );
        }
        return true;
    }

    public static boolean loadWorkspace(CommandSender commandSender, WorkspaceManager manager, String wsName) {
        if (wsName == null) wsName = manager.getWorkspaceName(commandSender.getName());
        if (manager.isWorkspaceLoaded(wsName)) {
            commandSender.sendMessage(
                    VarScriptPlugin.prefix + "workspace " + GREEN + wsName + RESET + " already loaded"
            );
        } else {
            Workspace workspace = manager.getWorkspace(wsName);
            if (workspace == null) {
                commandSender.sendMessage(
                        VarScriptPlugin.prefix + "can not load workspace " + GRAY + wsName
                );
            } else {
                commandSender.sendMessage(
                        VarScriptPlugin.prefix + "workspace " + GREEN + wsName + RESET + " loaded"
                );
            }

        }
        return true;
    }

    public static boolean unloadWorkspace(CommandSender commandSender, WorkspaceManager manager, String wsName) {
        if (wsName == null) wsName = manager.getWorkspaceName(commandSender.getName());
        if (manager.isWorkspaceLoaded(wsName)) {
            manager.unloadWorkspace(wsName);
            commandSender.sendMessage(
                    VarScriptPlugin.prefix + "workspace " + GRAY + wsName + RESET + " unloaded"
            );
        } else {
            commandSender.sendMessage(
                    VarScriptPlugin.prefix + "workspace " + GRAY + wsName + RESET + " not loaded yet"
            );
        }
        return true;
    }

    public static boolean setWorkspace(CommandSender commandSender, WorkspaceManager manager, String wsName, String user) {
        if (user == null || user.isEmpty()) user = commandSender.getName();
        manager.setWorkspaceName(user, wsName);
        boolean loaded = manager.isWorkspaceLoaded(wsName);
        commandSender.sendMessage(
                VarScriptPlugin.prefix + AQUA + user + RESET + " -> " +
                        (loaded ? GREEN : GRAY) + wsName
        );
        return true;
    }

    public static boolean setDefaultWorkspace(CommandSender commandSender, WorkspaceManager manager, String wsName, String user) {
        if (user == null || user.isEmpty()) user = commandSender.getName();
        manager.setDefaultWorkspaceName(user, wsName);
        boolean loaded = manager.isWorkspaceLoaded(wsName);
        commandSender.sendMessage(
                VarScriptPlugin.prefix + ChatColor.AQUA + user + ChatColor.RESET + " default workspace is " +
                        (loaded ? ChatColor.GREEN : ChatColor.GRAY) + wsName
        );
        return true;
    }

    public static boolean showWorkspaces(CommandSender commandSender, WorkspaceManager manager, String pattern) {
        StringBuilder builder = new StringBuilder();
        Set<String> workspaces = manager.getWorkspaces();
        List<String> readyWorkspaces = manager.getReadyWorkspaces();
        readyWorkspaces.removeAll(workspaces);
        builder.append(VarScriptPlugin.prefix).append("\nloaded workspaces:").append(GREEN);
        if (workspaces.isEmpty()) builder.append(GRAY).append(" none\n");
        for (String ws : workspaces) if (ws.contains(pattern)) builder.append('\n').append(ws);
        if (!readyWorkspaces.isEmpty()) builder.append(RESET).append("\nunloaded workspaces:").append(GRAY);
        for (String ws : readyWorkspaces) if (ws.contains(pattern)) builder.append('\n').append(ws);
        commandSender.sendMessage(builder.toString());
        return true;
    }
}
