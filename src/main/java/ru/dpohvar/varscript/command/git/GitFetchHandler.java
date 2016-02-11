package ru.dpohvar.varscript.command.git;

import net.md_5.bungee.api.ChatColor;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.transport.FetchResult;
import ru.dpohvar.varscript.caller.Caller;

public class GitFetchHandler implements GitResultHandler<FetchResult> {

    @Override
    public void handle(GitCommand<FetchResult> command, Caller caller, String callerWorkspaceName, FetchResult result) {
        caller.sendMessage("Fetch done. "+ ChatColor.AQUA+result.getMessages(), callerWorkspaceName);
    }
}
