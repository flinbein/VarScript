package ru.dpohvar.varscript.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.VarScriptPlugin;
import ru.dpohvar.varscript.WorkspaceManager;

/**
 * Created by DPOH-VAR on 24.02.14
 */
public class NamedEngineCommand implements CommandExecutor {

    private final WorkspaceManager manager;
    private final String lang;

    public NamedEngineCommand(WorkspaceManager manager, String lang){
        this.manager = manager;
        this.lang = lang;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) return false;
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<strings.length-1; i++) {
            builder.append(strings[i]).append(" ");
        }
        builder.append(strings[strings.length-1]);
        try {
            Object result = RunCodeCommand.execute(manager, commandSender, builder.toString(), lang );
            RunCodeCommand.sendResult(commandSender, result);
        } catch (Throwable e) {
            RunCodeCommand.sendException(commandSender, e);
        }
        return true;
    }

}
