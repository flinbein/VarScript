package ru.dpohvar.varscript.command;

import com.mysql.jdbc.StringUtils;
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

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by DPOH-VAR on 24.02.14
 */
public class RunScriptCommand implements CommandExecutor {

    private WorkspaceManager manager;
    private final File home;

    public RunScriptCommand(WorkspaceManager manager){
        this.manager = manager;
        home = new File(VarScriptPlugin.plugin.getDataFolder(), "scripts");
        if (!home.isDirectory() && !home.mkdirs()) {
            throw new RuntimeException("can not create folder "+home);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 0) return false;
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<strings.length-1; i++) {
            builder.append(strings[i]).append(" ");
        }
        builder.append(strings[strings.length-1]);
        try {
            Object result = execute(manager, commandSender, new File(home,builder.toString() ));
            if (result != null) commandSender.sendMessage(VarScriptPlugin.prefix + result);
        } catch (Throwable e) {
            RunCodeCommand.sendException(commandSender, e);
        }
        return true;
    }

    public static Object execute(WorkspaceManager manager, CommandSender sender, File file) throws Throwable {
        if (!file.isFile()) throw new PrintTextException("no script file "+file);
        String wsName = manager.getWorkspaceName(sender);
        Workspace ws = manager.getWorkspace(wsName);
        if (ws == null) throw new PrintTextException("can not load workspace "+ChatColor.GRAY+wsName);
        Object me = sender;
        if (sender instanceof BlockCommandSender) me = ((BlockCommandSender)sender).getBlock();
        return ws.runScript(me, file);
    }
}