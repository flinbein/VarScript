package ru.dpohvar.varscript.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorkspaceCommandCompleter implements TabCompleter {

    private static final List<String> commands = Arrays.asList("list", "set", "reload", "remove", "create","delete-files", "stop", "git", "autorun");
    private final VarScript plugin;

    public WorkspaceCommandCompleter(VarScript plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) return commands;
        String cmd = strings[0];
        if (strings.length == 1) {
            List<String> result = new ArrayList<String>();
            for (String c : commands) {
                if (c.startsWith(cmd)) result.add(c);
            }
            return result;
        }
        if (cmd.startsWith("git")) return completeGit(commandSender, strings);
        WorkspaceService service = plugin.getWorkspaceService();
        if (strings.length == 2) {
            List<String> result = new ArrayList<String>();
            if (cmd.equals("reload")||cmd.equals("remove")||cmd.equals("stop")||cmd.equals("set")||cmd.equals("list")||cmd.equals("clear")) {
                for (Workspace workspace : service.getWorkspaces()) {
                    String name = workspace.getName();
                    if (name.startsWith(strings[1])) result.add(name);
                }
            }
            if (cmd.equals("reload")||cmd.equals("create")||cmd.equals("list")||cmd.equals("delete-files")) {
                File[] files = service.getAutorunDirectory().listFiles();
                if (files != null) for (File file : files) {
                    if (!file.isFile()) continue;
                    String fileName = file.getName();
                    if (!fileName.endsWith(".groovy")||fileName.length()<8) continue;
                    String name = fileName.substring(0, fileName.length()-7);
                    if (!result.contains(name) && name.startsWith(strings[1])) result.add(name);
                }
                files = service.getServiceDirectory().listFiles();
                if (files != null) for (File file : files) {
                    if (!file.isDirectory()) continue;
                    String name = file.getName();
                    if (!result.contains(name) && name.startsWith(strings[1])) result.add(name);
                }
            }
            if (cmd.equals("autorun")){
                if ("add".startsWith(strings[1])) result.add("add");
                if ("remove".startsWith(strings[1])) result.add("remove");
            }
            return result;
        }
        if (strings.length == 3 && cmd.equals("autorun")){
            List<String> result = new ArrayList<String>();
            String pattern = strings[2];
            if (strings[1].matches("1|true|yes|y|on|\\+|enable|set|add")){
                File[] files = service.getAutorunDirectory().listFiles();
                if (files != null) for (File file : files) {
                    if (!file.isFile()) continue;
                    String fileName = file.getName();
                    if (!fileName.endsWith(".groovy")||fileName.length()<8) continue;
                    String name = fileName.substring(0, fileName.length()-7);
                    if (service.getWorkspaceAutorunState(name)) continue;
                    if (!result.contains(name) && name.startsWith(pattern)) result.add(name);
                }
                files = service.getServiceDirectory().listFiles();
                if (files != null) for (File file : files) {
                    if (!file.isDirectory()) continue;
                    String name = file.getName();
                    if (service.getWorkspaceAutorunState(name)) continue;
                    if (!result.contains(name) && name.startsWith(pattern)) result.add(name);
                }
            } else if (strings[1].matches("0|false|no|n|off|\\-|disable|stop|remove|rm")){
                for (String name : service.getWorkspaceAutoruns()) {
                    if (!result.contains(name) && name.startsWith(pattern)) result.add(name);
                }
            }
            return result;
        }
        return null;
    }

    private static final List<String> gitCommands = Arrays.asList("clone", "checkout", "branch", "tag", "log", "fetch", "delete");
    private List<String> completeGit(CommandSender commandSender, String[] strings) {
        String gitCommand = strings[1];

        if (strings.length == 2) {
            List<String> result = new ArrayList<String>();
            for (String c : gitCommands) {
                if (c.startsWith(gitCommand)) result.add(c);
            }
            return result;
        }

        Caller caller = plugin.getCallerService().getCaller(commandSender);
        WorkspaceService service = plugin.getWorkspaceService();
        String serviceName = service.getWorkspaceName(caller.getSender());

        if (gitCommand.equals("clone")){
            String url = strings[2];
            if (url.isEmpty()) return Collections.singletonList("<valid_git_url_here>");
            if (strings.length == 4) {
                try {
                    URIish urIish = new URIish(url);
                    return Collections.singletonList(urIish.getHumanishName());
                } catch (URISyntaxException e) {
                    caller.sendThrowable(e,serviceName);
                }
            }
            return null;
        }

        if (gitCommand.equals("delete") && strings.length == 3){
            List<String> result = new ArrayList<String>();
            String pattern = strings[2];
            File[] files = service.getServiceDirectory().listFiles();
            if (files != null) for (File file : files) {
                if (!file.isDirectory()) continue;
                File gitDir = new File(file, ".git");
                if (!gitDir.isDirectory()) continue;
                String name = file.getName();
                if (!result.contains(name) && name.startsWith(pattern)) result.add(name);
            }
            return result;
        }

        if (gitCommand.equals("branch") && strings.length == 3){
            return Arrays.asList("remote","all");
        }

        if (gitCommand.equals("fetch") && strings.length == 3){
            File gitDir = new File(service.getServiceDirectory(),serviceName+"/.git");
            if (!gitDir.isDirectory()) {
                caller.sendErrorMessage("No git here",serviceName);
                return null;
            }
            Git git = null;
            try {
                git = new Git(new FileRepository(gitDir));
                List<String> result = new ArrayList<String>();
                for (String remote : git.getRepository().getRemoteNames()) {
                    if (remote.startsWith(strings[2])) result.add(remote);
                }
                return result;
            } catch (Exception e){
                caller.sendThrowable(e,serviceName);
                return null;
            } finally {
                if (git != null) git.close();
            }
        }

        if (gitCommand.equals("checkout")||gitCommand.equals("log")) {
            String repo = strings[2];
            File gitDir = new File(service.getServiceDirectory(),serviceName+"/.git");
            if (!gitDir.isDirectory()) {
                caller.sendErrorMessage("No git here",serviceName);
                return null;
            }
            if (strings.length == 3) {
                Git git = null;
                try {
                    git = new Git(new FileRepository(gitDir));
                    List<String> result = new ArrayList<String>();
                    List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                    List<Ref> tags = git.tagList().call();
                    List<Ref> refs = new ArrayList<Ref>();
                    refs.addAll(branches);
                    refs.addAll(tags);
                    for (Ref ref : refs) {
                        String name = ref.getName();
                        if (name.startsWith("refs/remotes/")) name = name.substring(13);
                        if (name.startsWith("refs/heads/")) name = name.substring(11);
                        if (name.startsWith("refs/tags/")) name = name.substring(10);
                        if (name.startsWith(repo) && !result.contains(name)) result.add(name);
                    }
                    return result;
                } catch (Exception e) {
                    caller.sendThrowable(e,serviceName);
                    return null;
                } finally {
                    if (git != null) git.close();
                }
            }
        }

        if (gitCommand.equals("tag")||gitCommand.equals("log")) {
            String tagName = strings[2];
            File gitDir = new File(service.getServiceDirectory(),serviceName+"/.git");
            if (!gitDir.isDirectory()) {
                caller.sendErrorMessage("No git here",serviceName);
                return null;
            }
            if (strings.length == 3) {
                Git git = null;
                try {
                    git = new Git(new FileRepository(gitDir));
                    List<String> result = new ArrayList<String>();
                    List<Ref> tags = git.tagList().call();
                    ObjectId resolve = null;
                    for (Ref tag : tags) {
                        String name = tag.getName();
                        if (name.startsWith("refs/tags/")) name = name.substring(10);
                        if (name.startsWith(tagName) && !result.contains(name)) result.add(name);
                    }
                    return result;
                } catch (Exception e) {
                    caller.sendThrowable(e,serviceName);
                    return null;
                } finally {
                    if (git != null) git.close();
                }
            }
        }

        return null;
    }
}
