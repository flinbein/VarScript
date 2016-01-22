package ru.dpohvar.varscript.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
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

    static final List<String> commands = Arrays.asList("list", "set", "reload", "remove", "create", "stop", "git");
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
        if (strings.length == 2) {
            List<String> result = new ArrayList<String>();
            WorkspaceService service = plugin.getWorkspaceService();
            if (cmd.equals("reload")||cmd.equals("remove")||cmd.equals("stop")||cmd.equals("set")) {
                for (Workspace workspace : service.getWorkspaces()) {
                    String name = workspace.getName();
                    if (name.startsWith(cmd)) result.add(name);
                }
            }
            if (cmd.equals("reload")||cmd.equals("create")) {
                File[] files = service.getAutorunDirectory().listFiles();
                if (files != null) for (File file : files) {
                    String fileName = file.getName();
                    if (!fileName.endsWith(".groovy")||fileName.length()<8) continue;
                    String name = fileName.substring(0, fileName.length()-7);
                    if (!result.contains(name)) result.add(name);
                }
            }
            return result;
        }
        return null;
    }

    static final List<String> gitCommands = Arrays.asList("clone", "checkout", "branch", "pull", "fetch");
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
        if (gitCommand.equals("checkout")) {
            String repo = strings[2];
            File gitDir = new File(service.getServiceDirectory(),serviceName+"/.git");
            if (!gitDir.isDirectory()) {
                caller.sendErrorMessage("No git here",serviceName);
                return null;
            }
            if (strings.length == 3) try {
                Git git = new Git(new FileRepository(gitDir));
                List<String> result = new ArrayList<String>();
                List<Ref> branches = git.branchList().call();
                for (Ref branch : branches) {
                    String name = branch.getName();
                    name = name.substring(name.lastIndexOf('/')+1);
                    if (name.startsWith(repo)) result.add(name);
                }
                return result;
            } catch (Exception e) {
                caller.sendThrowable(e,serviceName);
                return null;
            }
        }
        return null;
    }
}
