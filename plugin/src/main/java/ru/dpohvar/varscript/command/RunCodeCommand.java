package ru.dpohvar.varscript.command;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.VarScriptPlugin;
import ru.dpohvar.varscript.Workspace;
import ru.dpohvar.varscript.WorkspaceManager;
import ru.dpohvar.varscript.exception.PrintTextException;
import ru.dpohvar.varscript.writer.CommandSenderWriter;

import javax.script.ScriptException;
import java.io.PrintWriter;

import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.RESET;
import static ru.dpohvar.varscript.VarScriptPlugin.*;

/**
 * Executor of command /code
 */
public class RunCodeCommand implements CommandExecutor {

    private WorkspaceManager manager;

    public RunCodeCommand(WorkspaceManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 2) return false;
        String lang = strings[0];
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < strings.length - 1; i++) {
            builder.append(strings[i]).append(" ");
        }
        builder.append(strings[strings.length - 1]);
        try {
            Object result = execute(manager, commandSender, builder.toString(), lang);
            sendResult(commandSender, result);
        } catch (Throwable e) {
            sendException(commandSender, e);
        }
        return true;
    }

    public static Object execute(WorkspaceManager manager, Object sender, String script, String lang) throws Throwable {
        String wsName = manager.getWorkspaceName(sender);
        Workspace ws = manager.getWorkspace(wsName);
        if (ws == null) throw new PrintTextException("can not load workspace " + ChatColor.GRAY + wsName);
        if (sender instanceof BlockCommandSender) sender = ((BlockCommandSender) sender).getBlock();
        return ws.runScript(sender, script, lang);
    }

    public static void sendResult(CommandSender sender, Object result) {
        if (result == null) return;
        String msg = result.toString();
        sender.sendMessage(prefix + msg);
    }

    public static void sendError(CommandSender sender, Object result) {
        if (result == null) return;
        String msg = result.toString();
        sender.sendMessage(errorPrefix + msg);
    }

    public static void sendException(CommandSender sender, Throwable e) {
        if (e instanceof PrintTextException) {
            sender.sendMessage(e.getMessage());
            return;
        }
        if (VarScriptPlugin.plugin.isPrintStackTraceToSender()) {
            PrintWriter writer = new PrintWriter(
                    new CommandSenderWriter(sender, VarScriptPlugin.errorPrefix)
            );
            e.printStackTrace(writer);
            return;
        }
        String message = e.getLocalizedMessage();
        if (message == null) message = e.getMessage();
        if (message == null) message = e.toString();
        if (plugin.isDebug()) e.printStackTrace();
        for (; ; ) {
            if (e.getCause() == null) break;
            if (e instanceof ScriptException) e = e.getCause();
        }
        sender.sendMessage(errorPrefix + RED + e.getClass().getSimpleName() + RESET + "\n" + message);
    }

}
