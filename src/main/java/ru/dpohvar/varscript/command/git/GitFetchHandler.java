package ru.dpohvar.varscript.command.git;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.transport.FetchResult;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;

public class GitFetchHandler implements GitResultHandler<FetchResult> {

    @Override
    public void handle(GitCommand<FetchResult> command, Caller caller, String callerWorkspaceName, FetchResult result) {
        String message = "Fetch done. " + ChatColor.AQUA + result.getMessages();
        VarScript plugin = caller.getService().getPlugin();
        Bukkit.getScheduler().runTask(plugin, new MessageSender(caller, message, callerWorkspaceName,0));
    }
}
