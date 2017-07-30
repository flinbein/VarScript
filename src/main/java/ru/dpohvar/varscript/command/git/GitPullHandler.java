package ru.dpohvar.varscript.command.git;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.PullResult;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;

public class GitPullHandler implements GitResultHandler<PullResult> {

    @Override
    public void handle(GitCommand<PullResult> command, Caller caller, String callerWorkspaceName, PullResult result) {
        VarScript plugin = caller.getService().getPlugin();
        if (result.isSuccessful()) {
            String message = "Merge result: " + ChatColor.AQUA + result.getMergeResult().getMergeStatus().toString();
            Bukkit.getScheduler().runTask(plugin, new MessageSender(caller, message, callerWorkspaceName,0));
        } else {
            String message = result.toString();
            Bukkit.getScheduler().runTask(plugin, new MessageSender(caller, message, callerWorkspaceName,1));
        }
    }
}
