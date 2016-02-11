package ru.dpohvar.varscript.command.git;

import org.bukkit.ChatColor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
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
    public void handle(GitCommand<Git> command, Caller caller, String callerWorkspaceName, Git result) {
        executor.onCommandGitCloneDone(caller, callerWorkspaceName, result, folderName);
    }
}
