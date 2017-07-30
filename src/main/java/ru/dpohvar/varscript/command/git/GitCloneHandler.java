package ru.dpohvar.varscript.command.git;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.command.WorkspaceCommandExecutor;

public class GitCloneHandler implements GitResultHandler<Git> {

    private final String folderName;
    private final WorkspaceCommandExecutor executor;

    public GitCloneHandler(WorkspaceCommandExecutor executor, String folderName) {
        this.executor = executor;
        this.folderName = folderName;
    }

    @Override
    public void handle(GitCommand<Git> command, final Caller caller, final String callerWorkspaceName, final Git result) {
        final VarScript plugin = caller.getService().getPlugin();
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                executor.onCommandGitCloneDone(caller, callerWorkspaceName, result, folderName);
            }
        });
    }
}
