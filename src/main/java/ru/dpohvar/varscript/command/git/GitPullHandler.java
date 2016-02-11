package ru.dpohvar.varscript.command.git;

import net.md_5.bungee.api.ChatColor;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.PullResult;
import ru.dpohvar.varscript.caller.Caller;

public class GitPullHandler implements GitResultHandler<PullResult> {

    @Override
    public void handle(GitCommand<PullResult> command, Caller caller, String callerWorkspaceName, PullResult result) {
        if (result.isSuccessful()) {
            caller.sendMessage("Merge result: "+ ChatColor.AQUA+result.getMergeResult().getMergeStatus().toString(), callerWorkspaceName);
        } else {
            caller.sendErrorMessage(result.toString(), callerWorkspaceName);
        }
    }
}
