package ru.dpohvar.varscript.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.command.git.*;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.*;

public class WorkspaceCommandExecutor implements CommandExecutor{

    private static final String gitUsage = ChatColor.translateAlternateColorCodes('&',"Git usage:" +
            "\n&e/ws git clone &6<url> &7[&6<name>&7]&r" +
            "\n&e/ws git branch &7[&eremote&7|&eall&7]&r" +
            "\n&e/ws git tag &7[&6<pattern>&7]&r" +
            "\n&e/ws git log &7[&6<ref>&7]&r" +
            "\n&e/ws git checkout &6<branch>&7|&6<commit>&r" +
            "\n&e/ws git fetch &6<remote>&r" +
            "\n&e/ws git delete-files &7[&6<name>&7]&r"
    );
    private static final String wsUsage = ChatColor.translateAlternateColorCodes('&',"Workspace usage:" +
            "\n&e/ws" +
            "\n&e/ws list &7[&6<pattern>&7]&r" +
            "\n&e/ws set &6<name>&r" +
            "\n&e/ws create &7[&6<name>&7]&r" +
            "\n&e/ws reload &7[&6<name>&7]&r" +
            "\n&e/ws stop &7[&6<name>&7]&r" +
            "\n&e/ws remove &7[&6<name>&7]&r" +
            "\n&e/ws delete &7[&6<name>&7]&r" +
            "\n&e/ws autorun &7[&eon&7|&eoff&7 [&6<name>&7]]&r" +
            "\n&e/ws git help&r"
    );
    private final VarScript plugin;

    public WorkspaceCommandExecutor(VarScript plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        Caller caller = plugin.getCallerService().getCaller(sender);
        if (strings.length == 0) return onCommandEmpty(caller);

        if (strings.length == 1 && strings[0].equals("list")) return onCommandList(caller, null);
        if (strings.length == 1 && strings[0].equals("remove")) return onCommandRemove(caller, null);
        if (strings.length == 1 && strings[0].equals("delete-files")) return onCommandDelete(caller, null);
        if (strings.length == 1 && strings[0].equals("reload")) return onCommandReload(caller, null);
        if (strings.length == 1 && strings[0].equals("create")) return onCommandCreate(caller, null);
        if (strings.length == 1 && strings[0].equals("stop")) return onCommandStop(caller, null);
        if (strings.length == 1 && strings[0].equals("autorun")) return onCommandAutorun(caller, null, null);

        if (strings.length == 2 && strings[0].equals("list")) return onCommandList(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("set")) return onCommandSet(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("remove")) return onCommandRemove(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("delete-files")) return onCommandDelete(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("reload")) return onCommandReload(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("create")) return onCommandCreate(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("stop")) return onCommandStop(caller, strings[1]);
        if (strings.length == 2 && strings[0].equals("autorun")) return onCommandAutorun(caller, strings[1], null);
        if (strings.length == 3 && strings[0].equals("autorun")) return onCommandAutorun(caller, strings[1], strings[2]);

        if (strings.length >= 1 && strings[0].equals("git")) {

            if (strings.length == 3 && strings[1].equals("clone")) return onCommandGitClone(caller, strings[2], null);
            if (strings.length == 4 && strings[1].equals("clone")) return onCommandGitClone(caller, strings[2], strings[3]);

            WorkspaceService service = plugin.getWorkspaceService();
            String workspaceName = service.getWorkspaceName(caller.getSender());

            if (strings.length == 1 || strings.length == 2 && strings[1].equals("help")){
                sender.sendMessage(gitUsage);
                return true;
            }

            File gitDir = new File(service.getServiceDirectory(),workspaceName+"/.git");
            if (!gitDir.isDirectory()) {
                caller.sendErrorMessage("No git here",workspaceName);
                return true;
            }
            Git git;
            try {
                git = new Git(new FileRepository(gitDir));
            } catch (IOException e) {
                caller.sendErrorMessage(e.getMessage(),workspaceName);
                return true;
            }

            if (strings.length == 3 && strings[1].equals("checkout")) return onCommandGitCheckout(caller, workspaceName, git, strings[2]);

            if (strings.length == 2 && strings[1].equals("branch")) return onCommandGitBranch(caller, workspaceName, git, null);
            if (strings.length == 3 && strings[1].equals("branch")) return onCommandGitBranch(caller, workspaceName, git, strings[2]);

            if (strings.length == 2 && strings[1].equals("tag")) return onCommandGitTag(caller, workspaceName, git, null);
            if (strings.length == 3 && strings[1].equals("tag")) return onCommandGitTag(caller, workspaceName, git, strings[2]);

            if (strings.length == 2 && strings[1].equals("fetch")) return onCommandGitFetch(caller, workspaceName, git, null);
            if (strings.length == 3 && strings[1].equals("fetch")) return onCommandGitFetch(caller, workspaceName, git, strings[2]);

            if (strings.length == 2 && strings[1].equals("delete")) return onCommandGitDelete(caller, null);
            if (strings.length == 3 && strings[1].equals("delete")) return onCommandGitDelete(caller, strings[2]);

            if (strings.length == 2 && strings[1].equals("log")) return onCommandGitLog(caller, workspaceName, git, null);
            if (strings.length == 3 && strings[1].equals("log")) return onCommandGitLog(caller, workspaceName, git, strings[2]);

            sender.sendMessage(gitUsage);
            return true;
        }

        sender.sendMessage(wsUsage);
        return true;
    }

    private boolean onCommandGitLog(Caller caller, String workspaceName, Git git, String ref) {
        try {
            ObjectId objectId = null;
            if (ref != null) {
                objectId = git.getRepository().resolve(ref);
                if (objectId == null) {
                    caller.sendErrorMessage("unresolved ref: "+ChatColor.RED+ref,workspaceName);
                    return true;
                }
            }
            LogCommand log = git.log();
            if (objectId != null) log = log.add(objectId);
            else log = log.all();
            Iterable<RevCommit> call = log.call();
            DateFormat dateFormat = DateFormat.getInstance();
            String message = "log"+(objectId==null?"":(" from "+ChatColor.AQUA+ref+ChatColor.RESET))+":";
            caller.sendMessage(message,workspaceName);
            for (RevCommit commit : call) {
                PersonIdent authorId = commit.getAuthorIdent();
                StringBuilder buf = new StringBuilder();
                String dateStr = dateFormat.format(authorId.getWhen());
                buf.append(ChatColor.AQUA).append(commit.getName().substring(0,7));
                buf.append(' ').append(ChatColor.YELLOW).append(dateStr);
                buf.append(' ').append(ChatColor.GREEN).append(authorId.getName());
                buf.append(' ').append(ChatColor.RESET).append(commit.getShortMessage());
                caller.getSender().sendMessage(buf.toString());
            }

        } catch (Exception e) {
            caller.sendThrowable(e,workspaceName);
            return true;
        } finally {
            git.close();
        }
        return true;
    }

    private boolean onCommandGitFetch(Caller caller, String workspaceName, Git git, String remote) {
        GitExecutor<FetchResult> executor = new GitExecutor<FetchResult>(caller, workspaceName, new GitFetchHandler());
        FetchCommand command = git.fetch()
                .setRemote(remote)
                .setProgressMonitor(executor)
                .setCredentialsProvider(executor);
        executor.setCommand(command);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,executor);
        return true;
    }

    private boolean onCommandGitBranch(Caller caller, String workspaceName, Git git, String mode){
        StringBuilder builder = new StringBuilder();
        try {
            ListBranchCommand.ListMode listMode = null;
            if (mode != null) {
                if (mode.toLowerCase().equals("all")) listMode = ListBranchCommand.ListMode.ALL;
                else if (mode.toLowerCase().equals("remote")) listMode = ListBranchCommand.ListMode.REMOTE;
                else {
                    caller.sendErrorMessage("Unknown list mode: "+mode,workspaceName);
                    return true;
                }
            }
            Repository repository = git.getRepository();
            String fullBranch = repository.getFullBranch();
            builder.append("branches:");
            List<Ref> branches = git.branchList().setListMode(listMode).call();
            for (Ref branch : branches){
                builder.append('\n');
                String name = branch.getName();
                ObjectId objectId = repository.resolve(name);
                if (name.equals("HEAD")){
                    builder.append(ChatColor.GREEN).append("* ").append(ChatColor.RESET).append("(detached HEAD at ")
                            .append(ChatColor.AQUA).append(objectId.getName().substring(0,7))
                            .append(ChatColor.RESET).append(")");
                    continue;
                }
                if (name.equals(fullBranch)) {
                    builder.append(ChatColor.RESET).append("* ").append(ChatColor.GREEN);
                } else {
                    builder.append("  ");
                }
                if (name.startsWith("refs/remotes/")) name = ChatColor.RED + name.substring(13);
                if (name.startsWith("refs/heads/")) name = ChatColor.GREEN + name.substring(11);
                builder.append(name)
                        .append(ChatColor.RESET)
                        .append(" - ")
                        .append(ChatColor.AQUA)
                        .append(objectId.getName().substring(0,7))
                        .append(ChatColor.RESET);
            }
        } catch (Exception e) {
            caller.sendThrowable(e,workspaceName);
            return true;
        } finally {
            git.close();
        }
        caller.sendMessage(builder,workspaceName);
        return true;
    }

    private boolean onCommandGitTag(Caller caller, String workspaceName, Git git, String pattern){
        StringBuilder builder = new StringBuilder();
        try {
            Repository repository = git.getRepository();
            builder.append("tags:");
            List<Ref> tags = git.tagList().call();
            for (Ref tag : tags){
                String name = tag.getName();
                String displayName = name;
                if (name.startsWith("refs/tags/")) displayName = name.substring(10);
                if (pattern != null && pattern.length() > 0) {
                    if (!displayName.startsWith(pattern)) continue;
                }
                ObjectId objectId = repository.resolve(name);
                builder.append("\n  ")
                        .append(ChatColor.YELLOW)
                        .append(displayName)
                        .append(ChatColor.RESET)
                        .append(" - ")
                        .append(ChatColor.AQUA)
                        .append(objectId.getName().substring(0,7))
                        .append(ChatColor.RESET);
            }
        } catch (Exception e) {
            caller.sendThrowable(e,workspaceName);
            return true;
        } finally {
            git.close();
        }
        caller.sendMessage(builder,workspaceName);
        return true;
    }

    private boolean onCommandGitCheckout(Caller caller, String workspaceName, Git git, String branch) {
        try {
            git.checkout().setName(branch).call();
            String fullBranch = git.getRepository().getFullBranch();
            caller.sendMessage("switched to "+ChatColor.AQUA+fullBranch,workspaceName);
        } catch (Exception e) {
            caller.sendErrorMessage(e.getMessage(),workspaceName);
            return true;
        } finally {
            git.close();
        }
        return true;
    }

    private boolean onCommandGitClone(Caller caller, String url, String folderName) {
        if (url.matches("[a-zA-Z0-9](?:-?[a-zA-Z0-9]){0,38}/[^/]+")) { // short github repo pattern
            url = "https://github.com/" +  url + ".git";
        }
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (folderName == null) try {
            folderName = new URIish(url).getHumanishName();
        } catch (URISyntaxException e){
            caller.sendErrorMessage(e.getMessage(),callerWorkspaceName);
            return true;
        }
        GitExecutor<Git> executor = new GitExecutor<Git>(caller, callerWorkspaceName, new GitCloneHandler(this,folderName));
        CloneCommand command = Git.cloneRepository()
                .setURI(url)
                .setRemote("origin")
                .setNoCheckout(true)
                .setCredentialsProvider(executor)
                .setProgressMonitor(executor)
                .setDirectory(new File(service.getServiceDirectory(), folderName));
        executor.setCommand(command);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,executor);
        return true;
    }

    public void onCommandGitCloneDone(Caller caller, String callerWorkspaceName, Git result, String folderName){
        Ref master;
        try {
            master = result.getRepository().findRef("origin/master");
        } catch (IOException e) {
            caller.sendThrowable(e,callerWorkspaceName);
            return;
        }
        if (master != null){
            try {
                String name = master.getName();
                result.checkout()
                        .setCreateBranch(false)
                        .setName(name)
                        .call();
                if (name.startsWith("refs/remotes/")) name = name.substring(13);
                String message = "Done: " + ChatColor.YELLOW + folderName + ChatColor.RESET +" now at "+ChatColor.AQUA+ name;
                caller.sendMessage(message, callerWorkspaceName);
            } catch (GitAPIException e) {
                caller.sendThrowable(e,callerWorkspaceName);
            }

        } else {
            try {
                List<Ref> refs = result.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
                StringBuilder buffer = new StringBuilder();
                buffer.append("Done: ").append(ChatColor.YELLOW).append(folderName);
                buffer.append(ChatColor.RESET).append(" now select branch:");
                if (!callerWorkspaceName.equals(folderName)){
                    buffer.append('\n').append(ChatColor.GOLD).append("/ws set ").append(folderName);
                }
                buffer.append('\n').append(ChatColor.GOLD).append("/ws checkout ").append(ChatColor.GREEN).append("branch");
                buffer.append('\n').append(ChatColor.RESET).append("Branches:");
                for (Ref ref : refs) {
                    buffer.append('\n').append(ChatColor.AQUA).append(ref.getName());
                }
                caller.sendMessage(buffer, callerWorkspaceName);
            } catch (GitAPIException e) {
                caller.sendThrowable(e,callerWorkspaceName);
            }
        }
    }

    private boolean onCommandGitDelete(Caller caller, String workspaceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (workspaceName == null) workspaceName = callerWorkspaceName;
        File gitDir = new File(service.getServiceDirectory(),workspaceName+"/.git");
        if (!gitDir.isDirectory()) {
            caller.sendErrorMessage("No git here",workspaceName);
            return true;
        }
        boolean success = deleteDir(gitDir);
        if (success) {
            caller.sendMessage("git of workspace " +ChatColor.YELLOW+ workspaceName+ChatColor.RESET + " are deleted", callerWorkspaceName);
        } else {
            caller.sendMessage("error on deleting " +ChatColor.YELLOW+ workspaceName+ChatColor.RESET + " git", callerWorkspaceName);
        }
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

    private boolean onCommandAutorun(Caller caller, String state, String workspaceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (workspaceName == null) workspaceName = callerWorkspaceName;
        Boolean autorunState = null;
        Boolean nowState = service.getWorkspaceAutorunState(workspaceName);
        if (state != null) {
            if (state.matches("1|true|yes|y|on|\\+|enable|set|add")) autorunState = true;
            if (state.matches("0|false|no|n|off|\\-|disable|stop|remove|rm")) autorunState = false;
        }
        if (autorunState == null) {
            String message = "autorun for " + ChatColor.YELLOW + workspaceName + ChatColor.RESET + " is " + (nowState ? "enabled" : "disabled");
            caller.sendMessage(message, callerWorkspaceName);
            return true;
        } else if (nowState == autorunState){
            String message = "autorun for " + ChatColor.YELLOW + workspaceName + ChatColor.RESET + " is already " + (autorunState ? "enabled" : "disabled");
            caller.sendMessage(message, callerWorkspaceName);
            return true;
        } else {
            service.setWorkspaceAutorunState(workspaceName, autorunState);
            String message = "autorun for " + ChatColor.YELLOW + workspaceName + ChatColor.RESET + " is " + (autorunState ? "enabled" : "disabled")+" now";
            caller.sendMessage(message, callerWorkspaceName);
            return true;
        }
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
            caller.sendMessage("workspace " + workspaceName + " is cleared", callerWorkspaceName);
        }
        return true;
    }

    private boolean onCommandDelete(Caller caller, String workspaceName) {
        WorkspaceService service = plugin.getWorkspaceService();
        String callerWorkspaceName = service.getWorkspaceName(caller.getSender());
        if (workspaceName == null) workspaceName = callerWorkspaceName;
        Workspace workspace = service.getWorkspace(workspaceName);
        if (workspace != null) {
            String message = "workspace " +ChatColor.YELLOW+ workspaceName+ChatColor.RESET + " is enabled!\n" +
                    "use \""+ChatColor.GOLD+"/ws remove "+workspaceName + ChatColor.RESET + "\" to disable workspace.";
            caller.sendErrorMessage(message, callerWorkspaceName);
            return true;
        }
        File autorunFile = new File(service.getAutorunDirectory(),workspaceName+".groovy");
        boolean hasFiles = false;
        boolean success = true;
        if (autorunFile.isFile() && Workspace.checkCanonicalName(autorunFile) != null){
            hasFiles = true;
            success = autorunFile.delete();
        }
        File autorunDir = new File(service.getServiceDirectory(),workspaceName);
        if (autorunDir.isDirectory() && Workspace.checkCanonicalName(autorunDir) != null){
            hasFiles = true;
            success &= deleteDir(autorunDir);
        }
        if (!hasFiles) {
            caller.sendMessage("workspace " +ChatColor.YELLOW+ workspaceName+ChatColor.RESET + " has no files", callerWorkspaceName);
        } else if (success) {
            caller.sendMessage("all files of workspace " +ChatColor.YELLOW+ workspaceName+ChatColor.RESET + " are deleted", callerWorkspaceName);
        } else {
            caller.sendErrorMessage("error on deleting " +ChatColor.YELLOW+ workspaceName+ChatColor.RESET + " files", callerWorkspaceName);
        }
        return true;
    }

    public static boolean deleteDir(File path){
        if (!path.exists()) return true;
        boolean ret = true;
        if (path.isDirectory()){
            File[] files = path.listFiles();
            if (files != null) for (File f : files){
                ret = ret && deleteDir(f);
            }
        }
        return ret && path.delete();
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

    public boolean onCommandList(Caller caller, String prefix) {
        WorkspaceService service = plugin.getWorkspaceService();
        String workspaceName = service.getWorkspaceName(caller.getSender());
        Set<String> activeWorkspaces = new HashSet<String>();
        Set<String> autorunWorkspaces = new HashSet<String>();
        Set<String> codeWorkspaces = new HashSet<String>();
        Set<String> gitWorkspaces = new HashSet<String>();
        Set<String> allWorkspaces = new TreeSet<String>();
        for (Workspace workspace : service.getWorkspaces()) {
            if (prefix == null || workspace.getName().startsWith(prefix)) {
                activeWorkspaces.add(workspace.getName());
                allWorkspaces.add(workspace.getName());
            }
        }
        for (String name : service.getWorkspaceAutoruns()) {
            if (prefix == null || name.startsWith(prefix)) {
                autorunWorkspaces.add(name);
                allWorkspaces.add(name);
            }
        }
        File[] files = service.getAutorunDirectory().listFiles();
        if (files != null) for (File file : files) {
            if (!file.isFile()) continue;
            String fileName = file.getName();
            if (!fileName.endsWith(".groovy")||fileName.length()<8) continue;
            String name = fileName.substring(0, fileName.length()-7);
            if (codeWorkspaces.contains(name)) continue;
            if (prefix != null && !name.startsWith(prefix)) continue;
            codeWorkspaces.add(name);
            allWorkspaces.add(name);
        }
        files = service.getServiceDirectory().listFiles();
        if (files != null) for (File file : files) {
            if (!file.isDirectory()) continue;
            String name = file.getName();
            if (codeWorkspaces.contains(name)) continue;
            if (prefix != null && !name.startsWith(prefix)) continue;
            codeWorkspaces.add(name);
            allWorkspaces.add(name);
            File gitDir = new File(file,".git");
            if (!gitDir.isDirectory()) continue;
            gitWorkspaces.add(name);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("workspaces:");
        for (String name : allWorkspaces) {
            builder.append("\n    ");
            String preColor = "";
            if (activeWorkspaces.contains(name)) {
                if (autorunWorkspaces.contains(name)) {
                    preColor += ChatColor.GREEN;
                    if (!codeWorkspaces.contains(name)){
                        preColor += ChatColor.STRIKETHROUGH;
                    }
                } else {
                    preColor += ChatColor.YELLOW;
                }
            } else {
                if (autorunWorkspaces.contains(name)){
                    preColor += ChatColor.RED;
                    if (!codeWorkspaces.contains(name)){
                        preColor += ChatColor.STRIKETHROUGH;
                    }
                } else {
                    preColor += ChatColor.GRAY;
                }
            }
            builder.append(preColor).append(name).append(ChatColor.RESET);
            builder.append("    (").append(activeWorkspaces.contains(name)?"enabled":"disabled");
            if (autorunWorkspaces.contains(name)) builder.append(",autorun");
            if (codeWorkspaces.contains(name)) builder.append(",script");
            if (gitWorkspaces.contains(name)) builder.append(",git");
            builder.append(")");

        }
        caller.sendMessage(builder.toString(), workspaceName);
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
