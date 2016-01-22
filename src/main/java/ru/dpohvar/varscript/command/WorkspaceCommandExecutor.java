package ru.dpohvar.varscript.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

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
        if (strings.length == 1 && strings[0].equals("service")) return onCommandServiceList(caller, null);

        if (strings.length == 2 && strings[0].equals("set")) return onCommandSet(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("remove")) return onCommandRemove(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("reload")) return onCommandReload(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("create")) return onCommandCreate(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("stop")) return onCommandStop(caller, strings[1]);


        if (strings.length >= 2 && strings[0].equals("git")) {
            if (strings.length == 3 && strings[1].equals("clone")) return onCommandGitClone(caller, strings[2], null);
            if (strings.length == 4 && strings[1].equals("clone")) return onCommandGitClone(caller, strings[2], strings[3]);

            WorkspaceService service = plugin.getWorkspaceService();
            String serviceName = service.getWorkspaceName(caller.getSender());
            File gitDir = new File(service.getServiceDirectory(),serviceName+"/.git");
            if (!gitDir.isDirectory()) {
                caller.sendErrorMessage("No git here",serviceName);
                return true;
            }
            Git git;
            try {
                git = new Git(new FileRepository(gitDir));
            } catch (IOException e) {
                caller.sendErrorMessage(e.getMessage(),serviceName);
                return true;
            }

            if (strings.length == 3 && strings[1].equals("checkout")) return onCommandGitCheckout(caller, serviceName, git, strings[2], null);
            if (strings.length == 4 && strings[1].equals("checkout")) return onCommandGitCheckout(caller, serviceName, git, strings[2], strings[3]);

            if (strings.length == 2 && strings[1].equals("branch")) return onCommandGitBranch(caller, serviceName, git);

            if (strings.length == 2 && strings[1].equals("pull")) return onCommandGitPull(caller, serviceName, git, null, null);
            if (strings.length == 3 && strings[1].equals("pull")) return onCommandGitPull(caller, serviceName, git, strings[2], null);
            if (strings.length == 4 && strings[1].equals("pull")) return onCommandGitPull(caller, serviceName, git, strings[2], strings[3]);

            if (strings.length == 2 && strings[1].equals("fetch")) return onCommandGitFetch(caller, serviceName, git, null);
            if (strings.length == 2 && strings[1].equals("fetch")) return onCommandGitFetch(caller, serviceName, git, null);

       }
        return false;
    }

    private boolean onCommandGitRemote(Caller caller, String serviceName, Git git) {
        try {
            Repository repository = git.getRepository();
            String remoteName = repository.getRemoteName(repository.getBranch());
            caller.sendMessage(remoteName, serviceName);
        } catch (Exception e) {
            caller.sendErrorMessage(e.getMessage(),serviceName);
            return true;
        }
        return true;
    }

    private boolean onCommandGitPull(Caller caller, String serviceName, Git git, String remote, String branch) {
        PullResult result;
        try {
            result = git.pull()
                    .setRemote(remote)
                    .setRemoteBranchName(branch)
                    .call();
        } catch (GitAPIException e) {
            caller.sendErrorMessage(e.getMessage(),serviceName);
            return true;
        }
        if (result.isSuccessful()) {
            caller.sendMessage(result.getMergeResult().getMergeStatus().toString(), serviceName);
        } else {
            caller.sendErrorMessage(result.toString(), serviceName);
        }
        return true;
    }


    private boolean onCommandGitFetch(Caller caller, String serviceName, Git git, String remote) {
        FetchResult result;
        try {
            result = git.fetch()
                    .setRemote(remote)
                    .call();
        } catch (GitAPIException e) {
            caller.sendErrorMessage(e.getMessage(),serviceName);
            return true;
        }
        caller.sendMessage(result.getMessages(), serviceName);
        return true;
    }

    private boolean onCommandGitBranch(Caller caller, String serviceName, Git git){
        StringBuilder builder = new StringBuilder();
        builder.append("branches:");
        try {
            String currentBranch = git.getRepository().getFullBranch();
            List<Ref> branches = git.branchList().call();
            for (Ref branch : branches){
                builder.append('\n');
                if (branch.getName().equals(currentBranch)) {
                    builder.append(ChatColor.RESET).append("* ").append(ChatColor.GREEN);
                } else {
                    builder.append("  ").append(ChatColor.RESET);
                }
                builder.append(branch.getName());
            }
        } catch (Exception e) {
            caller.sendErrorMessage(e.getMessage(),serviceName);
            return true;
        }
        caller.sendMessage(builder,serviceName);
        return true;
    }

    private boolean onCommandGitCheckout(Caller caller, String serviceName, Git git, String branch, String startPoint) {
        try {
            Ref result = git.checkout()
                    .setName(branch)
                    .setForce(true)
                    .setStartPoint(startPoint)
                    .call();
            if (result == null){
                caller.sendErrorMessage("revisions not supported",serviceName);
                return true;
            }
            caller.sendMessage("switched to "+result.getName(),serviceName);
        } catch (GitAPIException e) {
            caller.sendErrorMessage(e.getMessage(),serviceName);
            return true;
        }
        return true;
    }

    private boolean onCommandGitClone(Caller caller, String url, String serviceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (serviceName == null) try {
            URIish urish = new URIish(url);
            serviceName = urish.getHumanishName();
        } catch (URISyntaxException e){
            caller.sendErrorMessage(e.getMessage(),callerWorkspaceName);
            return true;
        }
        caller.sendPrintMessage("Cloning "+url+" into "+serviceName,callerWorkspaceName);
        try {
            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(new File(service.getServiceDirectory(), serviceName))
                    .setBare(false)
                    .setCloneAllBranches(true)
                    .call();
        } catch (GitAPIException e){
            caller.sendErrorMessage(e.getMessage(),callerWorkspaceName);
            return true;
        }
        caller.sendMessage("Cloning "+ serviceName+" done!",callerWorkspaceName);
        return true;
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

    public boolean onCommandServiceList(Caller caller, String pattern) {
        WorkspaceService service = plugin.getWorkspaceService();
        String workspaceName = service.getWorkspaceName(caller.getSender());
        File[] files = service.getServiceDirectory().listFiles();
        StringBuilder builder = new StringBuilder();
        builder.append("services:");
        List<String> autorunList = plugin.getConfig().getStringList("services.autorun");
        if (files != null) for (File file : files){
            if (!file.isDirectory()) continue;
            String fileName = file.getName();
            if (pattern != null && !fileName.startsWith(pattern)) continue;
            builder.append("\n");
            if (autorunList.contains(fileName)){
                builder.append(ChatColor.GREEN);
            } else {
                builder.append(ChatColor.GRAY);
            }
            builder.append(fileName);
        }
        caller.sendMessage(builder,workspaceName);
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
